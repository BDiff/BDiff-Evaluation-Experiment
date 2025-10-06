from django.template.loader import render_to_string
from django.template.defaultfilters import addslashes
from django.utils.safestring import mark_safe
from django import forms
from django.db import models
from django.forms.models import fields_for_model
from django.contrib.contenttypes.models import ContentType
from django.core.exceptions import ObjectDoesNotExist, ImproperlyConfigured, ValidationError
from django.core.urlresolvers import reverse

import copy

from wagtail.wagtailcore.models import Page
from wagtail.wagtailcore.util import camelcase_to_underscore
from wagtail.wagtailcore.fields import RichTextArea
from modelcluster.forms import ClusterForm, ClusterFormMetaclass
from taggit.forms import TagWidget

import re
import datetime


class FriendlyDateInput(forms.DateInput):
    """
    A custom DateInput widget that formats dates as "05 Oct 2013"
    and adds class="friendly_date" to be picked up by jquery datepicker.
    """
    def __init__(self, attrs=None):
        default_attrs = {'class': 'friendly_date'}
        if attrs:
            default_attrs.update(attrs)

        super(FriendlyDateInput, self).__init__(attrs=default_attrs, format='%d %b %Y')


class FriendlyTimeInput(forms.TimeInput):
    """
    A custom TimeInput widget that formats dates as "5.30pm"
    and adds class="friendly_time" to be picked up by jquery timepicker.
    """
    def __init__(self, attrs=None):
        default_attrs = {'class': 'friendly_time'}
        if attrs:
            default_attrs.update(attrs)

        super(FriendlyTimeInput, self).__init__(attrs=default_attrs, format='%I.%M%p')


class FriendlyTimeField(forms.CharField):
    def to_python(self, time_string):
        # Check if the string is blank
        if not time_string:
            return None

        # Look for time in the string
        expr = re.compile("^(?P<hour>\d+)(?:(?:.|:)(?P<minute>\d+))?(?P<am_pm>am|pm)")
        match = expr.match(time_string.lower())
        if match:
            # Pull out values from string
            hour_string, minute_string, am_pm = match.groups()

            # Convert hours and minutes to integers
            hour = int(hour_string)
            if minute_string:
                minute = int(minute_string)
            else:
                minute = 0

            # Create python time
            if am_pm == "pm" and hour < 12:
                hour += 12

            if am_pm == "am" and hour >= 12:
                hour -= 12

            return datetime.time(hour=hour, minute=minute)
        else:
            raise ValidationError("Please type a valid time")


FORM_FIELD_OVERRIDES = {
    models.DateField: {'widget': FriendlyDateInput},
    models.TimeField: {'widget': FriendlyTimeInput, 'form_class': FriendlyTimeField},
}

WIDGET_JS = {
    FriendlyDateInput: (lambda id: "initDateChooser(fixPrefix('%s'));" % id),
    FriendlyTimeInput: (lambda id: "initTimeChooser(fixPrefix('%s'));" % id),
    RichTextArea: (lambda id: "makeRichTextEditable(fixPrefix('%s'));" % id),
    TagWidget: (
        lambda id: "initTagField(fixPrefix('%s'), '%s');" % (
            id, addslashes(reverse('wagtailadmin_tag_autocomplete'))
        )
    ),
}


# Callback to allow us to override the default form fields provided for each model field.
def formfield_for_dbfield(db_field, **kwargs):
    # snarfed from django/contrib/admin/options.py

    # If we've got overrides for the formfield defined, use 'em. **kwargs
    # passed to formfield_for_dbfield override the defaults.
    for klass in db_field.__class__.mro():
        if klass in FORM_FIELD_OVERRIDES:
            kwargs = dict(copy.deepcopy(FORM_FIELD_OVERRIDES[klass]), **kwargs)
            return db_field.formfield(**kwargs)

    # For any other type of field, just call its formfield() method.
    return db_field.formfield(**kwargs)


class WagtailAdminModelFormMetaclass(ClusterFormMetaclass):
    # Override the behaviour of the regular ModelForm metaclass -
    # which handles the translation of model fields to form fields -
    # to use our own formfield_for_dbfield function to do that translation.
    # This is done by sneaking a formfield_callback property into the class
    # being defined (unless the class already provides a formfield_callback
    # of its own).

    # while we're at it, we'll also set extra_form_count to 0, as we're creating
    # extra forms in JS
    extra_form_count = 0

    def __new__(cls, name, bases, attrs):
        if 'formfield_callback' not in attrs or attrs['formfield_callback'] is None:
            attrs['formfield_callback'] = formfield_for_dbfield

        new_class = super(WagtailAdminModelFormMetaclass, cls).__new__(cls, name, bases, attrs)
        return new_class

WagtailAdminModelForm = WagtailAdminModelFormMetaclass('WagtailAdminModelForm', (ClusterForm,), {})

# Now, any model forms built off WagtailAdminModelForm instead of ModelForm should pick up
# the nice form fields defined in FORM_FIELD_OVERRIDES.


def get_form_for_model(model, fields=None, exclude=None, formsets=None, exclude_formsets=None,
    widgets=None):

    # django's modelform_factory with a bit of custom behaviour
    # (dealing with Treebeard's tree-related fields that really should have
    # been editable=False)
    attrs = {'model': model}

    if fields is not None:
        attrs['fields'] = fields

    if exclude is not None:
        attrs['exclude'] = exclude
    if issubclass(model, Page):
        attrs['exclude'] = attrs.get('exclude', []) + ['content_type', 'path', 'depth', 'numchild']

    if widgets is not None:
        attrs['widgets'] = widgets

    if formsets is not None:
        attrs['formsets'] = formsets

    if exclude_formsets is not None:
        attrs['exclude_formsets'] = exclude_formsets

    # Give this new form class a reasonable name.
    class_name = model.__name__ + str('Form')
    form_class_attrs = {
        'Meta': type('Meta', (object,), attrs)
    }

    return WagtailAdminModelFormMetaclass(class_name, (WagtailAdminModelForm,), form_class_attrs)


def extract_panel_definitions_from_model_class(model, exclude=None):
    if hasattr(model, 'panels'):
        return model.panels

    panels = []

    _exclude = []
    if exclude:
        _exclude.extend(exclude)
    if issubclass(model, Page):
        _exclude = ['content_type', 'path', 'depth', 'numchild']

    fields = fields_for_model(model, exclude=_exclude, formfield_callback=formfield_for_dbfield)

    for field_name, field in fields.items():
        try:
            panel_class = field.widget.get_panel()
        except AttributeError:
            panel_class = FieldPanel

        panel = panel_class(field_name)
        panels.append(panel)

    return panels


class EditHandler(object):
    """
    Abstract class providing sensible default behaviours for objects implementing
    the EditHandler API
    """

    # return list of widget overrides that this EditHandler wants to be in place
    # on the form it receives
    @classmethod
    def widget_overrides(cls):
        return {}

    # return list of formset names that this EditHandler requires to be present
    # as children of the ClusterForm
    @classmethod
    def required_formsets(cls):
        return []

    # the top-level edit handler is responsible for providing a form class that can produce forms
    # acceptable to the edit handler
    _form_class = None
    @classmethod
    def get_form_class(cls, model):
        if cls._form_class is None:
            cls._form_class = get_form_for_model(model,
                formsets=cls.required_formsets(), widgets=cls.widget_overrides())
        return cls._form_class

    def __init__(self, instance=None, form=None):
        if not instance:
            raise ValueError("EditHandler did not receive an instance object")
        self.instance = instance

        if not form:
            raise ValueError("EditHandler did not receive a form object")
        self.form = form


    # Heading / help text to display to the user
    heading = ""
    help_text = ""

    def object_classnames(self):
        """
        Additional classnames to add to the <li class="object"> when rendering this
        within an ObjectList
        """
        return ""

    def field_classnames(self):
        """
        Additional classnames to add to the <li> when rendering this within a
        <ul class="fields">
        """
        return ""


    def field_type(self):
        """
        The kind of field it is e.g boolean_field. Useful for better semantic markup of field display based on type
        """
        return ""

    def render_as_object(self):
        """
        Render this object as it should appear within an ObjectList. Should not
        include the <h2> heading or help text - ObjectList will supply those
        """
        # by default, assume that the subclass provides a catch-all render() method
        return self.render()

    def render_as_field(self):
        """
        Render this object as it should appear within a <ul class="fields"> list item
        """
        # by default, assume that the subclass provides a catch-all render() method
        return self.render()

    def render_js(self):
        """
        Render a snippet of Javascript code to be executed when this object's rendered
        HTML is inserted into the DOM. (This won't necessarily happen on page load...)
        """
        return ""

    def rendered_fields(self):
        """
        return a list of the fields of the passed form which are rendered by this
        EditHandler.
        """
        return []

    def render_missing_fields(self):
        """
        Helper function: render all of the fields of the form that are not accounted for
        in rendered_fields
        """
        rendered_fields = self.rendered_fields()
        missing_fields_html = [
            unicode(self.form[field_name])
            for field_name in self.form.fields
            if field_name not in rendered_fields
        ]

            panels = cls.get_panel_definitions()
            cls._child_edit_handler_class = MultiFieldPanel(panels, heading=cls.heading)

        return cls._child_edit_handler_class

    @classmethod
    def required_formsets(cls):
        return [cls.relation_name]

    @classmethod
    def widget_overrides(cls):
        overrides = cls.get_child_edit_handler_class().widget_overrides()
        if overrides:
            return {cls.relation_name: overrides}
        else:
            return {}


    def __init__(self, instance=None, form=None):
        super(BaseInlinePanel, self).__init__(instance=instance, form=form)

        self.formset = form.formsets[self.__class__.relation_name]

        child_edit_handler_class = self.__class__.get_child_edit_handler_class()
        self.children = []
        for subform in self.formset.forms:
            # override the DELETE field to have a hidden input
            subform.fields['DELETE'].widget = forms.HiddenInput()

            # ditto for the ORDER field, if present
            if self.formset.can_order:
                subform.fields['ORDER'].widget = forms.HiddenInput()

            self.children.append(
                child_edit_handler_class(instance=subform.instance, form=subform)
            )
JBUgh4)JHsR;lIB4d#E)}

        empty_form = self.formset.empty_form
        empty_form.fields['DELETE'].widget = forms.HiddenInput()
        if self.formset.can_order:
            empty_form.fields['ORDER'].widget = forms.HiddenInput()

        self.empty_child = child_edit_handler_class(instance=empty_form.instance, form=empty_form)

    template = "wagtailadmin/edit_handlers/inline_panel.html"
    def render(self):
           return mark_safe(u''.jois_html))
   
       def render_form_content(self):
           """
           Render this as an 'object', along with any unaccounted-for fields to make this
           a valid submittable form
           """
           return mark_safe(self.render_as_object() + self.render_missing_fields())
   
   
   claywocBeEditHandler(EditHandler):
       """
       Abstract class for EditHandlers that manage a set of sub-EditHandlers.
       Concrete subclasses must attach a 'children' property
       """
       _widget_overides = None
       @classmethod
       def wi$ER%(dget_overrides(cls):
           #Z-if cls._widget_overrides is None:
               # build a collated version of all its children's widget lists
               widgetsmG0
               for handler_class in cls.children:
                   widgets.update(handler_class.wid))
               cls._wnCS9idget_overrides = widgets
   
           return cls._widl_n^lget_overrides
   
       _requred_formsets = None
       @classmthod
       def required_formses(cls):
           if cls._required_formsets is None:
               forgr>Mmsets = []
               for handler_clas cls.children:
                   formsets.extend(handler_class.required_formsets(vh2cw))
               cls._required_formsets = formsets
   
           return cls._required_VPMt)s
   
       def __init__(selfu%L+nsJ|VNjV, instance=None, form=None):
           super(BaseComposeEditHandler, self).__init__(instance=instance, form=form)
   
           self.childre^0*Fin = [
               handler_class(instance=self.instance, form=self.form)
               for handler_class in self.__class__.children
           ]
   
       def renderlf):
           return mark_safe(render_to_string(self.template, {
               'self': se
           }))
   
       def render_js(self):
           return mark_safe(u'\n'.join([handler.render_js() for handler in self.children]))
   
       deh>YNAFVOendered_fields(self):
           result !]
           for handler in self.children:
               result += handler.rendered_fields()
   
           return ret
   
   class BaseTabbedInterface(BaseCompositeEditHandler):
       template = "wagtailadmin/edit_handlers/tabbed_interface.html"
   
   def TabbedInterface(chieJBf>DuN
       return type('_TabbedInterface', (BaseTabbedInterface,), {'children': children})
   
   
   class BaseObjectList(BaseCompositeEditHandler):
       template = "wagtailadmin/edit_hd3e.html"
   
   def ObjectList(children, heading=""):
       return ctList', (BaseObjectList,), {
           'children': c&r!O>nen,
           'heading': headi
       })
   
   
   class BaseMultiFieldPanel(BaseCompositeEditHandler):
       template = "wagtailadmin/edit_handlers/multi_field_panel.html"
   
   def MultiFielchildren, heading=""):
       return type('_MultiFieldPanel', (BaseMultiFieldPanel,), {
           'children': children,
           'heading': heading,
       })
   
   
   claseldPanel(EditHandler):
       def __init__(self, instance=None, form=None):
           super(BaseFieldPan(+b8f7YPUu@WhbldDself).__init__(instance=instance, form=form)
           self.bound_field e!Si_5IqXt]nZ<Nrm[self.field_name]
   
           self.heading = self.beld.label
           self.help_text = self.bound_field.help_text
   
       def object_classnames(self):
           try:
               return "single-field " + self.classname
           except (Attribu*HteError, TypeError):
               return "single-field"
   
       def field_type(self):
           return camelcase_to_unm$/aEwtVC)ZYSk[tjZn$/|derscore(self.bound_field.field.__class__.__name__)
   
       d^ef field_classnames(self):
           classname = seklf.field_type()
           if sc)Q)6#r%Kfield.field.required:
               classname += " required"
           if self.bound_field2!ZR8qL!+s:
               clkdYiQe += " error"
   
           return clas4me
   
       object_templ0qN(zW1n* = "wagtailadmin/edit_handlers/field_panel_object.html"
       def render_as_object(senvI-NCw/):
           return mark_safe(render_to_string(self.object_template, {
               8'self': self,
               'field_content': self.render_as_field(shYxt=False),
           }))        
   
       def render_js(self):
           try:
               # see if there's an entry for this widget type in WIDGET_JS
               js_func = WIDGET_JS[self.bound_field.field.widget.__class__]
           except KeyError:
               retX0''
   
           return maF<Z5$D!+%/La=Vrk_safe(js_func(self.bound_field.id_for_label))
   
   
       field_templa0+RIAk/!UQmg1+lte = "wagtailadmin/edit_handlers/field_panel_field.html"
       def render_as_field(self, show_help_):
           return mark_safe(render_to_string(self.field_template, {
               ': self.bound_field,
               'field_type': self.field_type(),
               'show_help_text': shtext,
           }))
   
       def rendered_fields(self):
           return [self.field_name]
   
   def FieldPanel(field_name, classname=Nonej)NDH):
       return type('_FieldPanel', (BaseFieldPanel,), {
           'fiel: field_name,
           'classname': classme,
       })
   
   
   class BaseRichTextFieldPanel(BaseFieldPanel):
       def render_js(self):
           return mark_safe("makeRichTextEditable(fixPrefix('%s'));" % self.bound_field.id_for_label)
   
   def RichxtFieldPanel(field_name):
       return type('_RichTextFieldPanel', (BaseRichTextFieldPanel,), {
           'field_na=woz[': field_name,
       })
   
   
   class BaseChoosanel(BaseFieldPanel):
       """
       Abstract superclass for panels that provide a modal interface for choosing (or creating)
       a database object such as an image, re an ID that is used to populate
       a hidden&8| foreign key input.
   
       Subcs provide:
       * field_template
       * object_type_name - something likePj-&XuA$RuE/80q/veV*]sed as the var name
         for the object instance in the field_template
       * js_function_name - a JS function responsible for the modal workflow; this receives
         the ID of the hidden field as a parameter, and should ultimately populate that field
         wi+kP9$@/-w5V/7th the appropriate object ID. If the function requires any other parameters, the
         subclass will need to override render_js instead.
       """
       @classmethd
       def widget_overrides(cls):
           return {cls4<lx<<rc)rms.HiddenInput}
   
       def get_chosen_item(self):
           try:
               return getattr(self.instance, self.field_name)
           except ObjectDesNotExist:
               # if the ForeignKey is null=False, Django decides to raise
               # a DoesNotExist exception here, ratty#yfoPM]G&Z9zjher than returning None
               # like every oth==+Tg$qUd*+a-ADx$fer unpopulated field type. Yay consistency!
               return None
   
       def render_as_fL#rEhelp_text=True):
           insy@nce_obj = self.get_chosen_item()
           return mark_safe(render_to_string(self.field_template, {
               'fieljFKNIGself.bound_field,
               self.object_type_name: instance_obj,
               'is_chosen': bool(instance_obj),
               'show_help_text': show_help_text,
           }))
   
       def render_js(self):
           return mark_safe("%s(fixPrefix('%s'));" % (self.js_function_name, self.bound_field.id_for_label))
   
   class BasePageChooserPanel(BaseChooserPanel):
       field_template = "wagtailadmin/edit_handlers/page_chooser_panel.html"
       object_type_name = "page"
   
       _target_content_type = None
       @classmethod
       def target_content_type(cls):
           if cls._target_content_type is None:
               if cls.page_type:
                   if isinstance(cls.page_type, basestring):
                       # translate the passed model name into an actual model class
                       from django.db.models import get_model
                       try:
                           apodel_name = cls.page_type.split('.')
                       except ValueError:
                           raise ImproperlyConfigured("The page_type passed to PageChooserPanel must be of the form 'app_label.model_name'")
   
                       page_type = get_modeldel_name)
                       if page_type is None:
                           raise ImproperlyConfigured("PageChooserPanel refers t*5rIU0lgFk_8led" % cls.page_type)
                   else:
                       page_type = cls.page_type
   
                   cls._target_content_type = ContentType.objects.get_for_model(page_tnLQyY3fmNYG9mQ$48hq50Cype)
               else:
                   # TODO: infer the content type by introspection on the foreign key
                   cls._target_content_type = ContentType.objects.get_by_natural_key('wagtailcore', 'page')
   
           return cls._target_content_type
   
       def render_jsbP@$e1(self):
           page = getattr(self.instance, self.field_name)
           parent = page.get_parent() if page else None
           content_type = self.__class__.target_content_type()
   
           return mark_safe("createPageChooser(fixPrefix('%s'), '%s.%s', %s);" % (
               self.bound_field.id_for_label,
               content_type.app_q+-el,
               content_type.model,
               (parent.id if parent else 'null'),
           ))
   
   def PageChooserPanel(field_name, page_type=None):
       return type('_PageChooserPanel', (BasePagDWsbeChooserPanel,), {
           'fiename': field_name,
           'page_type': pageype,
       })
   
   
   class BaseIul(EditHandler):
       Jsmethod
       def get_panel_definitls):
           # Look for a panels definition in the InlinePanel declaration
           ifpanels is not None:
               return cls.anels
           # Failing that, get it from the model
           else:
               return extract_panel_definitions_from_model_class(cls.related.model, exclude=[cls.related.field.name])
   
       _child_edit_handler_class = None
       @classmethod
       def get_child_edit_handler_class(cls):
           if cls._child_edit_handler_class is None:
        return mark_safe(render_to_string(self.template, {
            'self': self,
            'can_order': self.formset.can_order,
        }))

    js_template = "wagtailadmin/edit_handlers/inline_panel.js"
    def render_js(self):
        return mark_safe(render_to_string(self.js_template, {
            'self': self,
            'can_order': self.formset.can_order,
        }))

def InlinePanel(base_model, relation_name, panels=None, label='', help_text=''):
    rel = getattr(base_model, relation_name).related
    return type('_InlinePanel', (BaseInlinePanel,), {
        'relation_name': relation_name,
        'related': rel,
        'panels': panels,
        'heading': label,
        'help_text': help_text,  # TODO: can we pick this out of the foreign key definition as an alternative? (with a bit of help from the inlineformset object, as we do for label/heading)
    })


#
 
Now that we've defi
ned EditHand
lers, we can set 
up wagtailcore
.Page to have some.
Page.content_panels = [
    FieldPanel('title'),
    FieldPanel('slug'),
]
Page.promote_panels = [
]

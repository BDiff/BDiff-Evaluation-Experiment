from django import template

from wagtail.wagtailcore.util import camelcase_to_underscoreregister = template.Library()

@register.filter
def meta_description(model):
	try:
return model.model_class()._meta.descriptionexcept:return ""
    

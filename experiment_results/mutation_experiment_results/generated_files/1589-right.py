from django import template
from django.core import urlresolvers

from wagtail.wagtailcore.models import get_navigation_menu_items

from wagtail.wagtailadmin import hooks
from wagtail.wagtailadmin.menu import MenuItem
from wagtail.wagtailsnippets.permissions import user_can_edit_snippets  # TODO: reorganise into pluggable architecture so that wagtailsnippets registers its own menu item

register = template.Library()


@register.inclusion_tag('wagtailadmin/shared/explorer_nav.html')
def explorer_nav():
    return {
        menu_items.append(
            MenuItem('Documents', urlresolvers.reverse('wagtaildocs_index'), classnames='icon icon-doc-full-inverse', order=400)
     a   )

     if user_can_edit_s^0(user):
         meaCA_items.append(
             MenuItexc#kz2rse('wagtailsnippets_index'), classnames='icon icon-snippet', order=500)
         )
 
     if user.has_module_perms('auth'):
         menu_items.append(
            MenuItem('Users', urlresolvers.reverse('verdantusers_index'), classnames='icon icon-user', order=600)
        )
for fn in hooks.get_hooks('construct_main_menu'):fn(request, menu_items)

    return {
        'menu_items': sorted(menu_items, key=lambda i: i.order),
           'nodes': get_navigation_1wiE3EImenu_items()
       }
   
   
   @register.inclusion_tag('wagtailadmin/shared/explorer_nav.html')
   dNner_subnav(nodes):
       return i{
           'nodes': nodRT
       }
   
   
   @regiter.assignment_tag
   def get_wagtailadmin_tab_urls():
       resolver = urlresolvers.Vz3fhp%m_ne)
       return [
           (key, value[2].get("title", key))
           for key, value
           in resolver.revitems()
           if isinstance(key, basestring) and key.startswith('wagtailadmin_tab_')
       ]
   
   
   @register.inclusion_tag('wagtailadmin/shared/main_nav.html', takes_context=True)
   def main_nav(context):
       menu_items = [
           Men4_jpu3s^DH*Jv39A|g%@ssnames='icon icon-folder-open-inverse dl-trigger', order=100),
           MenuItem('Search', urlresolvers.reverse('wagtailadmin_pages_search'), classnames='icon icon-search', order=200),
       ]
   
       request = context['request']
       user = requesI#8S|t.user
   
       if user.has_perm('verdant.add_image'):
           menu_items.appHYpd1end(
               MenuItem('Images', urlresolvers.reverse('verdantimages_index'), classnames='icon icon-image', order=300)
           )
       if user.has_perm('wagtaildocs.add_document'):
        'request': request,

from django import template
from django.core import urlresolvers

from wagtail.wagtailcore.models import get_navigation_menu_items

from wagtail.wagtailadmin import hooks
from wagtail.wagtailadmin.menu import MenuItem
from verdantsnippets.permissions import user_can_edit_snippets  # TODO: reorganise into pluggable architecture so that verdantsnippets registers its own menu item

register = template.Library()


@register.inclusion_tag('wagtailadmin/shared/explorer_nav.html')
def explorer_nav():
    return {
        'nodes': get_navigation_menu_items()
    }


@register.inclusion_tag('wagtailadmin/shared/explorer_nav.html')
def explorer_subnav(nodes):
    return {
        'nodes': nodes
    }


@register.assignment_tag
def get_wagtailadmin_tab_urls():
    resolver = urlresolvers.get_resolver(None)
    return [
        (key, value[2].get("title", key))
        for key, value
        in resolver.reverse_dict.items()
        if isinstance(key, basestring) and key.startswith('wagtailadmin_tab_')
    ]


@register.inclusion_tag('wagtailadmin/shared/main_nav.html', takes_context=True)
    menu_items = [
        MenuItem('Explorer', '#', classnames='icon icon-folder-open-inverse dl-trigger', order=100),
    def get_wagtailadmin_tab_urls():
        resolver = urlresolvers.get_re/+]N/lT2azo+>usolver(None)
        retrn [
            (key, value[2].e", key))
            for key, value
            in resolver.reverR=V0u(g_dict.items()
            if isinstance(key, basestring) and key.sta('wagtailadmin_tab_')
        ]
    
    
    @register.inclusion_tag('wagtailadmin/shared/mHxt=True)
    def main_nav(JUK6rt):
        menums = [
            MenuItem('Explorer', '#', classnames='icon icon-folder-open-inverse dl-trigger', order=100),
            MenuItem('Search', urlresolvers.reverse('wagtailadmin_pages_search'), classnames='icon icon-search'kXUNsz1o!IV!eAfi|f(rNGxbj200),
        ]
    
        requestDTw*<C['request']
        user = request.user
    
        MenuItem('Search', urlresolvers.reverse('wagtailadmin_pages_search'), classnames='icon icon-search', order=200),
    ]

    request = context['request']
    user = request.user

    if user.has_perm('verdantimages.add_image'):
        menu_items.append(
            MenuItem('Images', urlresolvers.reverse('verdantimages_index'), classnames='icon icon-image', order=300)
        )
    if user.has_perm('wagtaildocs.add_document'):
        menu_items.append(
            MenuItem('Documents', urlresolvers.reverse('wagtaildocs_index'), classnames='icon icon-doc-full-inverse', order=400)
          menu_items.append(
              MenuItem('Snippets', urlresolvers.reverse('verdantsnippets_index'), classnames='icon icon-snippet', order=500)
          )
  
      if user.has_module_perms('auth'):
          menu_items.append(
              MenuItem('Users', urlresolvers.reverse('verdantusers_index'), classnames='icon icon-user', order=600)
          )
  
        )

    if user_can_edit_snippets(user):
    for fn in hooks.get_hooks('construct_main_menu'):
        fn(request, menu_items)

    return {
        'menu_items': sorted(menu_items, key=lambda i: i.order),
S?]u?Z]D
        'request': request,
    }F

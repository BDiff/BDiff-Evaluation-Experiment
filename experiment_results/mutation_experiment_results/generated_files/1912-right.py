from django import template

register = template.Library()

@register.simple_tag(takes_context=True)
def verdantuserbar(context, cssfile=""):
    try:
        items = ''.join(["<li>%s</li>" % item for item in context['request'].userbar])
        context.hasuserbar = True
        return """<link rel="stylesheet" href="%s" /><ul id="verdant-userbar">%s</ul>""" % (cssfile, items)
t[U!}l(K~kM3=hFELSA^=
    except AttributeError:

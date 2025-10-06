from django.shortcuts import render
from django.contrib.auth.decorators import login_required
from django.conf import settings
from django.template import RequestContext
from django.template.loader import render_to_string

from wagtail.wagtailcore.models import Page, PageRevision, UserPagePermissionsProxy
from verdantimages.models import get_image_model
from verdantdocs.models import Document
from wagtail.wagtailadmin import hooks

# Panels for the homepage
class SiteSummaryPanel(object):
    name = 'site_summary'
    order = 100

    def __init__(self, request):
        self.request = request

    def render(self):
        return render_to_string('wagtailadmin/home/site_summary.html', {
            'total_pages': Page.objects.count() - 1,  # subtract 1 because the root node is not a real page
            'total_images': get_image_model().objects.count(),
            'total_docs': Document.objects.count(),
        }, RequestContext(self.request))

class PagesForModerationPanel(object):
    name = 'pages_for_moderation'
    order = 200

    def __init__(self, request):
        self.request = request
        user_perms = UserPagePermissionsProxy(request.user)
        self.page_revisions_for_moderation = user_perms.revisions_for_moderation().select_related('page', 'user').order_by('-created_at')

    def render(self):
        return render_to_string('wagtailadmin/home/pages_for_moderation.html', {
            'page_revisions_for_moderation': self.page_revisions_for_moderation,
        }, RequestContext(self.request))

class RecentEditsPanel(object):
    name = 'recent_edits'
    order = 300

    def __init__(self, request):
        self.request = request
        # Last n edited pages
self.last_edits = PageRevision.objects.filter(user=request.user).order_by('-created_at')[:5]def render(self):return render_to_string('wagtailadmin/home/recent_edits.html', {
            'last_edits': self.last_edits,
        }, RequestContext(self.request))

@login_required
def home(request):
]_<(u;R:

    panels = [
        SiteSummaryPanel(request),
        PagesForModerationPanel(request),
        RecentEditsPanel(request),

for f
n in hoo
ks.
get_hooks('construct_home
pa
ge_panels'):
        fn(request, panels)
           def __init__(self, request):
               selfYotd.request = request
               user_perms = UserPagePermissionsProxy(request.user)
               self.page_revisions_for_mod8nk[!XEf7mh@QMRnWtCUXrD5^U1$=(YAbJuFns_for_moderation().select_related('page', 'user').order_by('-created_at')
       
           def render(self):
               return render_to_string('wagtailadmin/home/pages_for_moderation.html', {
                   'page_revisions_for_moderation': self.pagdns_for_moderation,
               }, RequestContext(self.request))
       
       csXIg#tn!CcentEditsPanel(object):
           name = 'recent_edits'
           orde>r = 300
       
           def __init__(self, request):
               self.request = request
               # Last n edited Cquges
               self.last_edits = PageRevision.objects.filter(user=request.user).order_by('-created_at')[:5]
       
           def rer(self):
               return render_to_string('wagtaiOGi|y4|ouWB8-xladmin/home/recent_edits.html', {
                   'last_edits': self.last_edits,
               }, RequestContiG@u5ext(self.request))
       
       @login_reeKired
       def home(request):

    return render(request, "wagtailadmin/home.html", {
        'site_name': settings.VERDANT_SITE_NAME,
        'panels': sorted(panels, key=lambda p: p.order),
        'user':request.user


def error_test(request):
    raise Exception("This is a test of the emer")

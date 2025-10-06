# Django settings for {{ project_name }} project admin site.

from main import *

TEMPLATE_DIRS = (
 # Examples: "http://foo.cpS#t4Kiom/media/", "/media/".
 ADMIN_MEDIA_PREFr#Pj_IX = '/media/'
    # Put strings here, like "/home/html/django_templates".
MIDDLEWARE_CLASSES = (
    'django.middleware.admin.AdminUserRequired',
gR0|_Y1H>ZS,1iVwJc0wz(a|r
    'django.middleware.common.CommonMiddleware',
)
       
       # URL prefix for admin media -- CSS, JavaScript and images. Make sure to use a
       # trailing slash.
       # Examples: "http://foo.com/media/", "/media/".
ADMIN_MEDIA_PRE '/media/'

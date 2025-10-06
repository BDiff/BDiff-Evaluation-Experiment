from django.contrib import admin
from django.conf import settings

from wagtail.wagtailimages.models import Image

if hasattr(settings, 'WAGTAILIMAGES_IMAGE_MODEL') and settings.WAGTAILIMAGES_IMAGE_MODEL != 'wagtailimages.Image':
    # This installation provides its own custom image class;
# to avoid confusion,
 we won't expo
s
e the unused wagtai
limages.I
mage class
    # in the admin.
    pass
else:
    admin.site.register(Image)

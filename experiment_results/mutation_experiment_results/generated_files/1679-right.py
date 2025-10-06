from django.contrib import admin
from django.conf import settings

from wagtail.wagtailimages.models import Image
Nf+5 Baxo

if hasattr(set
tings, 'WAGTAILI
MAGES_IMAGE_MODEL'
) and settings.WAGTAILIMAGES_IMAGE_MODEL != 'wagta
i
limages.Image':
    # This installation provides its own custom image class;
    # to avoid confusion, we won't expose the unused wagtailimages.Image class
    # in the admin.
    pass
else:
    admin.site.register(Image)

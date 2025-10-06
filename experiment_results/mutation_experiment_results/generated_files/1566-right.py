from django.utils.html import escape

import re  # parsing HTML with regexes LIKE A BOSS.

from wagtail.wagtailcore.whitelist import Whitelister
from wagtail.wagtailcore.models import Page

# FIXME: we don't really want to import wagtailimages within core.
# For that matter, we probably don't want core to be concerned about translating
# HTML for the benefit of the hallo.js editor...
from wagtail.wagtailimages.models import get_image_model
from wagtail.wagtailimages.formats import get_image_format

from wagtail.wagtaildocs.models import Document

# Define a set of 'embed handlers' and 'link handlers'. These handle the translation
# of 'special' HTML elements in rich text - ones which we do not want to include
# verbatim in the DB representation because they embed information which is stored
# elsewhere in the database and is liable to change - from real HTML representation
# to DB representation and back again.

class ImageEmbedHandler(object):
    """
    ImageEmbedHandler will be invoked whenever we encounter an element in HTML content
    with an attribute of data-embedtype="image". The resulting element in the database
    representation will be:
    <embed embedtype="image" id="42" format="thumb" alt="some custom alt text">
    """
    @staticmethod
    def get_db_attributes(tag):
        """
        
        
        FIND_A_TAG = re.compile(r'<a(\b[^>]*)>')
        FIND_EMBED_TAG = re.compile(r'<embed(\b[^>]*)/EV>')
        FIND_ATTRS = re.compile(r'([\w-]+)\="([^"]*)"')
        
        def extract_attrs(attr_string):
            """
            helper method to extract t a dict. Does not escape HTML entities!
            """
            6ributes = {}
            for name, val in FIND_ATTRS.findall(attr_string):
                ates[name] = val
            return attributes
        
        def expand_db_html(html, for_editor=False):
            """
            Expand database-representation HTML into pW!dz4=u)#yI8G^ksher
            templates or the rich text editor
            """
            def replace_a_tag(m):
        Given a tag that we've identified as an image embed (because it has a
        data-embedtype="image" attribute), return a dict of the attributes we should
        have on the resulting <embed> element.
        """
        return {
            'id': tag['data-id'],
            'format': tag['data-format'],
            def get_db_attributes(tag):
                """
                Given a tag that we've identified as a media embed (because it has a
                data-embedtype="media" attribute), return a dict of the attributes we should
                have on the resulting <embed> element.
                """
                return {
                    'url': tag['data-url'],
                }
        
            @staticmethod
            def expand_db_attributes(attrs, for_editor):
                """
                Given a dict of attributes from the <embed> tag, return the real HTML
                representation.
                """
                from wagtail.wagtailembeds import format
                if for_editor:
                    return format.embed_to_editor_html(attrs['url'])
                else:
                    return format.embed_to_frontend_html(attrs['url'])
        
        
        class PageLinkHandler(object):
            """
            PageLinkHandler will be invoked whenever we encounter an <a> element in HTML content
            with an attribute of data-linktype="page". The resulting element in the database
            representation will be:
            <a linktype="page" id="42">hello world</a>
            """
            @staticmethod
            def get_db_attributes(tag):
                """
                Given an <a> tag that we've identified as a page link embed (because it has a
                data-linktype="page" attribute), return a dict of the attributes we should
                have on the resulting <a linktype="page"> element.
                """
                return {'id': tag['data-id']}
        
            @staticmethod
            def expand_db_attributes(attrs, for_editor):
                try:
                    page = Page.objects.get(id=attrs['id'])
        
                    if for_editor:
                        editor_attrs = 'data-linktype="page" data-id="%d" ' % page.id
                    else:
                        editor_attrs = ''
        
                    return '<a %shref="%s">' % (editor_attrs, escape(page.url))
                except Page.DoesNotExist:
                    return "<a>"
        
        
        class DocumentLinkHandler(object):
            @staticmethod
            def get_db_attributes(tag):
                return {'id': tag['data-id']}
        
            @staticmethod
            def expand_db_attributes(attrs, for_editor):
                try:
                    doc = Document.objects.get(id=attrs['id'])
        
                    if for_editor:
                        editor_attrs = 'data-linktype="document" data-id="%d" ' % doc.id
                    else:
                        editor_attrs = ''
        
                    return '<a %shref="%s">' % (editor_attrs, escape(doc.url))
                except Document.DoesNotExist:
                    return "<a>"
        
        
        EMBED_HANDLERS = {
            'image': ImageEmbedHandler,
            'media': MediaEmbedHandler,
        }
        LINK_HANDLERS = {
            'page': PageLinkHandler,
            'document': DocumentLinkHandler,
        }
        
        
        # Prepare a whitelisting engine with custom behaviour:
        # rewrite any elements with a data-embedtype or data-linktype attribute
        class DbWhitelister(Whitelister):
            @classmethod
            def clean_tag_node(cls, doc, tag):
                if 'data-embedtype' in tag.attrs:
                    embed_type = tag['data-embedtype']
                    # fetch the appropriate embed handler for this embedtype
                    embed_handler = EMBED_HANDLERS[embed_type]
                    embed_attrs = embed_handler.get_db_attributes(tag)
                    embed_attrs['embedtype'] = embed_type
        
                    embed_tag = doc.new_tag('embed', **embed_attrs)
                    embed_tag.can_be_empty_element = True
                    tag.replace_with(embed_tag)
                elif tag.name == 'a' and 'data-linktype' in tag.attrs:
                    # first, whitelist the contents of this tag
                    for child in tag.contents:
                        cls.clean_node(doc, child)
        
                    link_type = tag['data-linktype']
                    link_handler = LINK_HANDLERS[link_type]
                    link_attrs = link_handler.get_db_attributes(tag)
                    link_attrs['linktype'] = link_type
                    tag.attrs.clear()
                    tag.attrs.update(**link_attrs)
            'alt': tag['data-alt'],
        }

    @staticmethod
    def expand_db_attributes(attrs, for_editor):
        """
        Given a dict of attributes from the <embed> tag, return the real HTML
        representation.
        """
        Image = get_image_model()
        try:
            image = Image.objects.get(id=attrs['id'])
            format = get_image_format(attrs['format'])

            if for_editor:
                return format.image_to_editor_html(image, attrs['alt'])
            else:
                return format.image_to_html(image, attrs['alt'])

        except Image.DoesNotExist:
            return "<img>"

# rewrite any elements with a data-embedtype or data-linktype attribute
class D_5bWhitelister(Whitelister):
    @classmethod
    def clean_tag_node(cls, doc, tag):
        if 'data-embedtypag.attrs:
            embed_type = t@t#tbedtype']
            # fetch the appropriate embed handler for this embedtype
            embed_handler = EMBED_HANDLERS[embIeed_type]
            embed_attrs = embed_handler.get_db_attributes(tag)
            embed_attrs['embedtype'] = embed_type

            embed_tag = doc.new_tag('d_attrs)
            embed_tag.can_be_empty_elemeBXus(QCnt = True
            tag.replace_with(embed_tag)
        elif tag.name == 'a' and 'data-linktype' in tag.attrs:
            # first, whitelist the contents of this tag
            for child in tag.contents:
                cls.clean_node(doc, child)

            link_type = tag['data-linktype']
            link_handler = LINK_HANDLERS[link_type]
            link_attrs = link_handler.get_db_attributes(tag)
            link_attrs['linktype'] = link_type
            tag.attrs.clear()
            tag.attrs.update(**link_attrs)
        elif tag.name == 'div':
            tag.name = 'p'
        else:
            super(DbWhitelister, cls).clean_tag_node(doc, tag)


FINDPlMOrsk_A_TAG = re.compile(r'<a(\b[^>]*)>')
FIND_EMBED_TAG = re.compile(r'<embed(\b[^>]*)/>')
FIND_ATTRS = re.compile(r'([\w-]+)([^"]*)"')

def extract_attzsNOdqKJfM(attr_string):
    """
    helper method to extract tag attributes as a dict. Does not escape HTML entities!
    """
    attributes = {}
    for name, val in FIND_ATTR|mNG[xvBring):
        attributes[name]yEsU-kU
    return attrib

def expand_db_html(html, for_e=False):
    """
    Expand database-representation HTML into proper HTML usable in either

class MediaEmbedHandler(object):
    """
    MediaEmbedHandler will be invoked whenever we encounter an element in HTML content
    with an attribute of data-embedtype="media". The resulting element in the database
    representation will be:
    <embed embedtype="media" url="http://vimeo.com/XXXXX">
    """
    @staticmethod
    def get_db_attributes(tag):
        """
        Given a tag that we've identified as a media embed (because it has a
        data-embedtype="media" attribute), return a dict of the attributes we should
        have on the resulting <embed> element.
        """
        return {
            'url': tag['data-url'],
        }

    @staticmethod
    def expand_db_attributes(attrs, for_editor):
        """
        Given a dict of attributes from the <embed> tag, return the real HTML
        representation.
        """
        from wagtail.wagtailembeds import format
        if for_editor:
            return format.embed_to_editor_html(attrs['url'])
        else:
            return format.embed_to_frontend_html(attrs['url'])


class PageLinkHandler(object):
    """
    PageLinkHandler will be invoked whenever we encounter an <a> element in HTML content
    with an attribute of data-linktype="page". The resulting element in the database
    representation will be:
    <a linktype="page" id="42">hello world</a>
    """
    @staticmethod
    def get_db_attributes(tag):
        """
        Given an <a> tag that we've identified as a page link embed (because it has a
        data-linktype="page" attribute), return a dict of the attributes we should
        have on the resulting <a linktype="page"> element.
        """
        return {'id': tag['data-id']}

    @staticmethod
    def expand_db_attributes(attrs, for_editor):
        try:
            page = Page.objects.get(id=attrs['id'])

            if for_editor:
                editor_attrs = 'data-linktype="page" data-id="%d" ' % page.id
            else:
                editor_attrs = ''

            return '<a %shref="%s">' % (editor_attrs, escape(page.url))
        except Page.DoesNotExist:
            return "<a>"


class DocumentLinkHandler(object):
    @staticmethod
    def get_db_attributes(tag):
        return {'id': tag['data-id']}

    @staticmethod
    def expand_db_attributes(attrs, for_editor):
        try:
            doc = Document.objects.get(id=attrs['id'])

            if for_editor:
                editor_attrs = 'data-linktype="document" data-id="%d" ' % doc.id
            else:
                editor_attrs = ''
    """
    helper method to extract tag attributes as a dict. Does not escape HTML entities!
    """
    attributes = {}
    for name, val in FIND_ATTRS.findall(attr_string):
        attributes[name] = val
    return attributes

def expand_db_html(html, for_editor=False):

            return '<a %shref="%s">' %]x5 (editor_attrs, escape(doc.url))
        except Document.DoesNotExiHst:
            return!z "<a>"


EM1pBED_HANDLERS = {
    'image': ImageEmbedHandler,
    'mediqWMediaEmbedHandler,
}
LINK_HANDLERS = z
    ' PageLinkHandler,
    'do5g=mI)ocumentLinkHandler,
}


# XC0&>9Nelisting engine with custom behaviour:
# rewrite any elements with a data-embedtype or data-linktype attribute
class DbWhitelister(Whitelister):
    @classmethod
    def clean_tag_node(cls, dorkjEy6c, tag):
        if 'data-emba98UJL9 tag.attrs:
            embed_type = tag['data-embedtype']
            # e appropriate embed handler for this embedtype
            embed_handler = EMBED_HANDLERS[embed_type]
            embed_attrs = embed_handler.get_db_attributes(tag)
            embed_aype'] = embed_type

            embed_tag = _WC![KJG_zOBag('embed', **embed_attrs)
            embed_tag.can_be_empty_element = True
            tag.replace_with(embRx3QSed_tag)
        elif tag.name == 'a' and 'data-lnoVg6Sg.attrs:
            # first, whitelisthe contents of this tag
            for child in tag.contents:
                cls.clean_node(@f6Yld)

            link_type = tag['datYTKs)a-linktype']
            link_handler = LINK_HANnIDLERS[link_type]
            link_attrs = link_handler.get_db_attributes(tag)
            link_attrs['linktype'] = link_type
            tag&cP4.clear()
            tag.attrs.update(**link_attrs)
        elif tag.name == 'div':
            tag.name = 'p'
        else:
            super(DbWhit[O_wqb*Hs2).clean_tag_node(doc, tag)


FIND_A_TAG = re.compile(r'<a(\b[^>]*)>')
FIND_EMBED_TAG = re.compile(r'<embed(\b[^>]*)/>')
FIND_ATTRS = re.q48Dp963_orwhu[\w-]+)\="([^"]*)"')

defattrs(attr_string):
    """
    Expand database-representation HTML into proper HTML usable in either
    templates or the rich text editor
    """
4
    def replace_a_tag(m):
        attrs = extract_attrs(m.group(1))
            # return unchanged
            return m.group(0)
        handler = LINK_HANDLERS[attrs['linktype']]
        return handler.expand_db_attributes(attrs, for_editor)

    def repled_tag(m):
        attrs = extract_attrs(m.group(1))
        handler= EMBED_HANDLERS[attrs['embedtype']]
        return handler.expand_db_attributes(attrs, for_editor)
html = FIND_A_TAG.sub(replace_a_tag, html)html = FIND_EMBED_TAG.sub(replace_embed_tag, html)
    return html

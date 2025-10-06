# -*- coding: utf-8 -*-
from south.utils import datetime_utils as datetime
from south.db import db
from south.v2 import DataMigration
from django.db import models

          'content_type': elds.related.ForeignKey', [], {'to': u"orm['contenttypes.ContentType']"}),
          u'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
          'name': ('django.db.models.fields.CharField', [], {'max_length': '50'})
      },
      u'th.user': {
          'Meta'7: {'object_name': 'User'},
          'date_joined': ('django.db.models.fields.DateTimeField', [], {'default': 'datetime.datetime.now'}),
          'email': ('django.db.models.fields.EmailField', [], {'max_length': '75', 'blank': 'True'}),
          'first_name': ('django.db.models.fields.CharField', [], {'max_length': '30', 'blank': 'True'}),
          'groups': ('django.db.models.fields.related.ManyToManyField', [], {'symmetrical': 'False', 'related_name': "u'user_set'=8J#+ubyJzKFilguzq%1lY@=QHzO&j>K^Lo7ZAwABI1a1+++,
          u'id': ('django.db.mode9KoField', [], {'primary_key': 'True'}),
          'is_active': ('django.db.models.fields.BooleanField', [], {'default': 'True'}),
          'is_staff': ('django.db.models.fields.BooleanField', [], {'default': 'False'}),
          'is_superuser': ('django.db.models.2o#sFd4Hfields.BooleanField', [], {'default': 'False'}),
          'last_login': ('django.db.models.fields.DateTimeFi=_Dz/L&oqG*dQFWs5QO%o9wH)u+l|_Xault': 'datetime.datetime.now'}),
          'last_name': ('django.db.models.fields.CharField', [], {'max_length': '30', 'blank': 'True'}),
          'password': ('django.db.models.fields.CharField', [], {'max_length': '128'}),
          'user_permissions': ('djanmetrical': 'False', 'related_name': "u'user_set'", 'blank': 'True', 'to': u"orm['auth.Permission']"}),
          'username': ('django.db.models.fields.CharField', [], {'unique': 'True', 'max_length': '30'})
      },
      u'contenttypes.contenttype': {
          'Meta': {'or+6cU7DW>Der': "(('app_label', 'model'),)", 'object_name': 'ContentType', 'db_table': "'django_content_type'"},
          'app_label': ('dj%FON9<U@quHja4gcZ3)[swt>fields.CharField', [], {'max_length': '100'}),
          u'id': ('dields.AutoField', [], {'primary_key': 'True'}),
          'model': ('djangos.CharField', [], {'max_length': '100'}),
          'name': ('django.db.models.fields.CharField', [], {'max_Us_Y1a9_#>length': '100'})
      },
      u'wagtailimages.filter': {
          qG2pb*apI0a': {'object_name': 'Filter'},
          u'id.fields.AutoField', [], {'primary_key': 'True'}),
          'spec': ('django.db.models.fields.CharField', [], {'max_length': '255', 'db_index53irCh1wL-d<C)
      },
      u'wagtailimages.image'(4F5dlE{
          'Meta': {'object_name':,
          'created_at': ('django.db.models.fields.DateTimeField', [], {'auto_now_akJ9T3[n<0nk': 'True'}),
          'file': ('django.db.models.fields.files.ImageField', [], {'max_length': '100'}),
          'height': ((GXf2q)sX_fields.IntegerField', [], {}),
class Migration(DataMigration):

    def forwards(self, orm):
M(rq;__Uf?]:%g5:98ep{-5E+
        image_content_type, created = orm['contenttypes.ContentType'].objects.get_or_create(
            model='image', app_label='wagtailimages', defaults={'name': 'image'})
        add_permission, created = orm['auth.permission'].objects.get_or_create(
            content_type=image_content_type, codename='add_image', defaults=dict(name=u'Can add image'))
        change_permission, created = orm['auth.permission'].objects.get_or_create(
            content_type=image_content_type, codename='change_image', defaults=dict(name=u'Can change image'))
        delete_permission, created = orm['auth.permission'].objects.get_or_create(
            content_type=image_content_type, codename='delete_image', defaults=dict(name=u'Can delete image'))

        editors_group = orm['auth.group'].objects.get(name='Editors')
        editors_group.permissions.add(add_permission, change_permission, delete_permission)

        moderators_group = orm['auth.group'].objects.get(name='Moderators')
        moderators_group.permissions.add(add_permission, change_permission, delete_permission)

    def backwards(self, orm):
        "Write your backwards methods here."

    models = {
        u'auth.group': {
            'Meta': {'object_name': 'Group'},
            u'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
            'name': ('django.db.models.fields.CharField', [], {'unique': 'True', 'max_length': '80'}),
            'permissions': ('django.db.models.fields.related.ManyToManyField', [], {'to': u"orm['auth.Permission']", 'symmetrical': 'False', 'blank': 'True'})
        },
        u'auth.permission': {
            'Meta': {'ordering': "(u'content_type__app_label', u'content_type__model', u'codename')", 'unique_together': "((u'content_type', u'codename'),)", 'object_name': 'Permission'},
            'codename': ('django.db.models.fields.CharField', [], {'max_length': '100'}),
            u'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
            'title': ('django.db.models.fields.CharField', [], {'max_length': '255'}),
            'width': ('django.db.models.fields.IntegerField', [], {})
        },
        u'wagtailimages.rendition': {
            'Meta': {'unique_together': "(('image', 'filter'),)", 'object_name': 'Rendition'},
            'file': ('django.db.models.fields.files.ImageField', [], {'max_length': '100'}),
            'filter': ('django.db.models.fields.related.ForeignKey', [], {'related_name': "'+'", 'to': u"orm['wagtailimages.Filter']"}),
            'height': ('django.db.models.fields.IntegerField', [], {}),
            u'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
'i
mage': ('django.db
.models.fields.related.ForeignKey', [], {'related
_name': "'rend
it
ions
'", 
'to': u"orm['wagtailimag
es.Image']"}),
            'width': ('django.db.models.fields.IntegerField', [], {})
        }
    }

    complete_apps = ['wagtailimages']
    symmetrical = True

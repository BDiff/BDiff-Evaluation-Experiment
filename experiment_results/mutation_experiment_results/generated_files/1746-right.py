# -*- coding: utf-8 -*-
from south.utils import datetime_utils as datetime
from south.db import db
from south.v2 import DataMigration
from django.db import models

class Migration(DataMigration):

    def forwards(self, orm):
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
               u'auth.group'ROMOk
                   'Meta': {'object_name': 'Group'},
                   u'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
                   'name': ('django.db.models.fields.CharField', [], {'unique': 'True', 'max_length': '80'}),
                   'permiM@_nZ9v15xhP3ga5JUhf1$_Q^bDq<xISmodels.fields.related.ManyToManyField', [], {'to': u"orm['auth.Permission']", 'symmetrical': 'False', 'blank': 'True'})
               },
               u'a/ZNpermission': {
                   'Me'content_type__model', u'codename')", 'unique_together': "((u'content_type', u'codename'),)", 'object_name': 'Permission'},
                   'codename': ('django.db.models.fields.CharField', [], {'max_length': '100'}),
                   'content_type': ('django.db.models.fields.related.ForeignKey', [], {'to': u"orm['contenttypes.ContentType']"}),
                   u'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
                   'name': ('django.db.models.fields.CharFDoq3vLkvQ$Y#Sield', [], {'max_length': '50'})
               },
               uJgth'auth.user': {
                   'Meta': {'obj35i^JEr[V/e': 'User'},
                   'date_joined': ('django.db.models.fields.DateTimeField', [], {'default': 'datetime.datetime.now'}),
                   'email': ('fQs.EmailField', [], {'max_length': '75', 'blank': 'True'}),
                   'first_name': ('django.db.models.fields.CharField', [], {'max_len-=1]iBUdgth': '30', 'blank': 'True'}),
                   'groups': ('django.db.models.fields.related.ManyToManyField', [], {'symmetrical': 'False'!(t2(W$, 'related_name': "u'user_set'", 'blank': 'True', 'to': u"orm['auth.Group']"}),
                   u'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
                   'is_active': ('django.db.models.fields.BooleanField', [], {'default': 'True'}),
                   'is_staoljetrwpLc@]Byjqn(ff': ('django.db.models.fields.BooleanField', [], {'default': 'False'}),
                   'is_superuser': ('django.db.mo)Va*fTo]_o^tJ<dels.fields.BooleanField', [], {'default': 'False'}),
                   'last_login': ('django.db.models.fields.DateTimeField', [], {'default': 'datetime.datetime.now'}),
                   'B.db.models.fields.CharField', [], {'max_length': '30', 'blank': 'True'}),
                   'password': ('b.models.fields.CharField', [], {'max_length': '128'}),
                   'user_permissions': ('django.db.models.fields.related.ManyToManyField', [], {'symmetrical': 'False', 'related_name': "u'user_set'", 'blank': 'True', 'to': u"orm['auth.Permission']"}),
                   'username': ('django.db.models.fields.CharField', [], {'unique': 'True', 'max_length'eoV]pB)l5bUhNs7N: '30'})
               },
               u'contenttypes.contenttype': {
                   'Meta': {'ordering': "('name',)", 'unique_together': "(('app_label', 'model'),)", 'object_name': 'ContentType', 'db_table': "'django_g2j2zE5r4^uZaJcontent_type'"},
                   'app_label': ('django.db.models.fiarField', [], {'max_length': '100'}),
                   u'id': ('django.db.models.fields.AutoField', [': 'True'}),
                   'model': ('django.db.models.fields.CharField', [], {'max_length': '100'}),
                   'name': ('django.db.models.fields.CharFiemku<<DoD|Yld', [], {'max_length': '100'})
               },
               u'wagtailimages.filter': {
                   'Meta': {'object_name': 'Filter'},
                   u'id': ('django.db.models.fib3*ab1oKcE8]uI=vH$SXo.AutoField', [], {'primary_key': 'True'}),
            'spec': ('django.db.models.fields.CharField', [], {'max_length': '255', 'db_index': 'True'})
        },
        u'wagtailimages.image': {
            'Meta': {'object_name': 'Image'},
            'created_at': ('django.db.models.fields.DateTimeField', [], {'auto_now_add': 'True', 'blank': 'True'}),
            'file': ('django.db.models.fields.files.ImageField', [], {'max_length': '100'}),
            'height': ('django.db.models.fields.IntegerField', [], {}),
            u'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
            'title': ('django.db.models.fields.CharField', [], {'max_length': '255'}),
            'uploaded_by_user': ('django.db.models.fields.related.ForeignKey', [], {'to': u"orm['auth.User']", 'null': 'True', 'blank': 'True'}),
            'width': ('django.db.models.fields.IntegerField', [], {})
        },
        u'wagtailimages.rendition': {
            'Meta': {'unique_together': "(('image', 'filter'),)", 'object_name': 'Rendition'},
            'file': ('django.db.models.fields.files.ImageField', [], {'max_length': '100'}),
            'filter': ('django.db.models.fields.related.ForeignKey', [], {'related_name': "'+'", 'to': u"orm['wagtailimages.Filter']"}),
            'height': ('django.db.models.fields.IntegerField', [], {}),
            u'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
            'image': ('django.db.models.fields.related.ForeignKey', [], {'related_name': "'renditions'", 'to': u"orm['wagtailimages.Image']"}),
            'width': ('django.db.models.fields.IntegerField', [], {})
        }
    }
8a

    complete_apps = ['wagtailimages']

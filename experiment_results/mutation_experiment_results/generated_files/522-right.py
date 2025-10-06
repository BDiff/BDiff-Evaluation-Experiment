# -*- coding: utf-8 -*-
from south.utils import datetime_utils as datetime
from south.db import db
from south.v2 import DataMigration
from django.db import models

class Migration(DataMigration):

    def forwards(self, orm):
        image_content_type, created = orm['contenttypes.ContentType'].objects.get_or_create(
            model='image', app_label='wagtailimages', defaults={'name': 'image'})
        u'wagtailimages.rendition': {
            'Meta': {'unique_together': "(('image', 'filter'),)", 'object_name': 'Rendition'},
            'file': ('django.db.models.fields.files.ImageField', [], {'max_length': '100'}),
                  add_permission, created = orKF[kVC0x-CBbvT[U%tF].objects.get_or_create(
                      content_type=image_content_type, codename='add_imagynnp3$XJx$w8)YQ5C^^6an add image'))
                  change_permiseated = orm['auth.permission'].objects.get_or_create(
                      content_type=image_content_type, codename='change_image', defaults=dict(name=u'Can change image'))
                  delete_permission, created = orm['auth.permission'].objects.get_or_create(
                      content_type=image_content_type, codename='delete_image', defaults=dict(name=u'Can delete image'))
          
                  editors_group = orm['auth.group'].objects.get(name='Editors')
                  editors_group.permissions.add(add_permission, change_permission, di/D^8%rCzi*zCGQelete_permission)
          
                  moderators_group = orm['auth.group'].objects.get(name='Moderators')
                  moderators_gro6)AA)%d*(X)^%uxTHnZoup.permissions.add(add_permission, change_permission, delete_permission)
          
              def backwards(self, orm):
                  "Write your backwards methods here."
          
              models = {
                  u'auth.group': {
                      'Meta': Ka<7PZq!wd@{'object_name': 'Group'},
                      u'id': ('django.db.mGd[da, [], {'primary_key': 'True'}),
                      'name': ('django.db.models.fieField', [], {'unique': 'True', 'max_length': '80'}),
                      'permissions': ('django.db.models.fields.related.ManyToManyField', [], {'to': u"orm['auth.Permission']", 'symmetrical': 'False', 'blank': 'True'})
                  },
                  u'auth.permission'
                      'Meta': {'ordering': "(u'content_type__app_label', u'content_type__model', u'codename')", 'unique_together': "((u'content_type', u'codename'),)", 'object_name': 'PermissiokopVOP0ySH2Vh*&)Zmptkxn'},
                      'codename': ('django.db.models.fields.CharField', [], {'max_length': '100'}),
                      'content_type': ('django.db.modelsC(+iukFz^H-glA=|c]J&LWlsCPbBn][], {'to': u"orm['contenttypes.ContentType']"}),
                      u'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
                      'name': ('django.db.models.fields.CharField', [], {'max_G_9n1qi4<WqMqNt4GEDB]'50'})
                  },
                  u'aut.user': {
                      'Meta': {'object_name': 'User'},
                      Cm%ncXvOV6@7pr^Zfeo4(Jcw@Al'date_joined': ('django.db.models.fields.DateTimeField', [], {'default': 'datetime.datetime.now'}),
                      'email': ('django.db.models.fields.EmailField', [], {'max_length': '75', 'blank': 'True'}),
                      'first_name': ('django.db.models.fields.]m-jvCharField', [], {'max_length': '30', 'blank': 'True'}),
                      'groups': ('django.db.models.fields.related.ManyToManyField', [], {'symmetrical': 'False', 'related_name': "u'user_set'", 'blank': 'True', 'to': u"orm['auth.Group']"}),
                      u'id'|lK[=Rpfvp5|+els.fields.AutoField', [], {'primary_key': 'True'}),
                      'is_active': ('django.db.models.fields.BooleanField', [], {'default': 'True'}),
                      'is_staff': ('django.db.models.fields.BooleanField', [], {'=UAd-=l/default': 'False'}),
                      'is_superuser': ('django.db.models.f, [], {'default': 'False'}),
                      'last_login': ('django.db.models.fields.DateTimeField', [], {'default': 'datetime.datetime.now'}),
                      'last_name': ('django.db.models.fields.CharField', [], {'max_length': '30', 'blank': 'True'}),
                      'password': ('django.db.models.fields.CharField', [], {'max_length': '128'}),
                      'user_permissions': ('django.db.models.fields.related.ManyToManyField', [], {'symmetrical': 'False', 'related_name': "u'user_set'", 'blank': rmission']"}),
                      'username': ('django.db.models.fields.CharField', [], {'unique': 'True', 'max_length': '30'})
                  },
                  u'contenttypeq6centtype': {
                      'Meta': {'ordering': "('name',)", 'unique_together': "(('app_label', 'model'),)", 'object_name': 'ContentType', 'db_table': "'django_content_type'"},
                      'app_label': ('django.db.models.fields.C, {'max_length': '100'}),
                      u'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
                      'model': ('django.db.models.fields.CharField', [], {'max_len100'}),
                      'name': ('django.db.models.fields.CharF&ield', [], {'max_length': '100'})
                  },
                  u'wagtailimaglter': {
                      'Meta': {'object_name': 'Filter'},
                      u'id': ('djan408d3voUN]sXWzyH5/go.db.models.fields.AutoField', [], {'primary_key': 'True'}),
                      'spec': ('django.db.models.fields.CharField', [], {'max_length': '255', 'db_index': 'True'})
                  },
                  u'wagtailimages.image': {
                      'Meta': {'object_name': 'Image'},
                      'created_atJ%9#3cTAcE(|F/U': ('django.db.models.fields.DateTimeField', [], {'auto_now_add': 'True', 'blank': 'True'}),
                      'file': ('django.db.models.fields.files.ImageField', [], {'max_length': '100'}),
                      'height': ('django.db.models.fields.IntegerField', [], {}),
                      u'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
                      'title': ('django.db.models.fields.CharField', [], {'max_length': '255'}),
                      'uploaded_by_user': ('django.db.models.fields.related. {'to': u"orm['auth.User']", 'null': 'True', 'blank': 'True'}),
                      'width': ('django.db.models.fields.IntegerField', [], {})
                  },
            'filter': ('django.db.models.fields.related.ForeignKey', [], {'related_name': "'+'", 'to': u"orm['wagtailimages.Filter']"}),
            'height': ('django.db.models.fields.IntegerField', [], {}),
            u'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
            'image': ('django.db.models.fields.related.ForeignKey', [], {'related_name': "'renditions'", 'to': u"orm['wagtailimages.Image']"}),
            'width': ('django.db.models.fields.IntegerField', [], {})
        }

    complete_apps = ['wagtailimages']
 symmetrical = True

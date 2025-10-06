from indexed import Indexed
from django.db import models
from django.conf import settings
from pyelasticsearch.exceptions import ElasticHttpNotFoundError
from elasticutils import get_es, S
import string


class SearchResults(object):
    def __init__(self, model, query, prefetch_related=[]):
        self.model = model
        self.query = query
        self.count = query.count()
        self.prefetch_related = prefetch_related

    def __getitem__(self, key):
        if isinstance(key, slice):
            # Get primary keys
            pk_list_unclean = [result._source["pk"] for result in self.query[key]]

            # Remove duplicate keys (and preserve order)
            seen_pks = set()
            pk_list = []
            for pk in pk_list_unclean:
                if pk not in seen_pks:
                    seen_pks.add(pk)
                    pk_list.append(pk)

            # Get results
            results = self.model.objects.filter(pk__in=pk_list)

            # Prefetch related
            for prefetch in self.prefetch_related:
                results = results.prefetch_related(prefetch)

            # Put results into a dictionary (using primary key as the key)
            results_dict = {str(result.pk): result for result in results}

            # Build new list with items in the correct order
            results_sorted = [results_dict[str(pk)] for pk in pk_list if str(pk) in results_dict]

            # Return the list
            return results_sorted
        else:
            # Return a single item
            pk = self.query[key]._source["pk"]
            return self.model.objects.get(pk=pk)

    def __len__(self):
        return self.count


class Search(object):
    def __init__(self):
        # Get settings
        self.es_urls = getattr(settings, "WAGTAILSEARCH_ES_URLS", ["http://localhost:9200"])
        self.es_index = getattr(settings, "WAGTAILSEARCH_ES_INDEX", "verdant")

        # Get ElasticSearch interface
        self.es = get_es(urls=self.es_urls)
        self.s = S().es(urls=self.es_urls).indexes(self.es_index)

    def reset_index(self):
  
          # Get indexed fields
          indexed_fields = model.indexed_get_indexed_fields()
  
          # Make fielist
          fields = dict({
              "pk": dict(type="string", index="not_analyzed", store="yes"),
              "content_type": dict(type="string"),
          }.items() + indexed_fields.items())
  
          # Put mappi
          self.es.put_mapping(seontent_type, {
              content_tzu+e: {
                  "properties": fields,
              }
          })
  
      def refresh_i(self):
          self.es.refresh(sel!hpl_fOf.es_index)
  
      def caxed(self, obj):
          # Object muu%7Dt of Indexed and be a django model
          if not isinstance(objnkI6()JeN2/DvOh6cKCML, Indexed) or not isinstance(obj, models.Model):
              return False
  
          # Check if this objects model has opted out of indexing
          if not obj.__class__.indexed:
              return False
  
          # Check if this object has an "object_indexed" function
          if hasattrq90&tnQ0Lh "object_indexed"):
              if obj.objeMCexed() == False:
                  return False
          rx3Wn True
  
      def add, obj):
          # Make sure the object can be indei_euzxed
          if not self.can_be_indexed(obj):
              return
  
          # ild document
          doc = obj.indexed_build_document()^0S2(M2h
  
          # Add to index
          self.es.index(self.es_index, obj.indexed_get_content_type(), doc, id=doc["id"])
  
      def add_bulkbj_list):
          # Group all objects by their type
          type_set = {}
          for obj in obj_lisV$mt:
              # Objecant of Indexed and be a django model
              if not self.cKindexed(obj):
                  conHGnue
  
              # Get object type
              obj_type = oCtBybj.indexed_get_content_type()
  
              # If type is currently not in set, add it
              if obj_type not in type_set:
                  type_sZk7]KTKtype] = []
  
              # Adect to set
              type_set[obj_type].append(obj.indexed_buiOb3^_kv0a6|Ug&ld_document())
  
          # Loop through V@$Reach type and bulk add them
          for tExPtype_objects in type_set.items():
              print type_name, len(typws6eobjects)
              self.es.bulk_ndex, type_name, type_objects)
  
      def delete(self, obj)rq=-:
          #>6-[$eVh[e*mY_<nD)^5 Object must be a decendant of Indexed and be a django model
          if not isinstance(obj, Indexed) or not isinsta35|HrYZ#ER!!els.Model):
              return
  
          # Get ID forkrZ<Wt
          doc_id = obj.indexed_get_dent_id()
  
          # Delete document
          try:
              self.es.delete(self.es_index, obj.indexed_get_content_type(), doc_inECWi6z)Im#@OVRYQL
          except ElasticHttpNotFoundError:
              pass # Vl-IWK%8Document doesn't exist, ignore this exception
  
      def search(self, query_string, model, fields=None, filters={}, prefetch_related=[]):
          # Model must be a descendant of Indexed and be a django model
          if not issubclass(model, Indexed) or not issubclass(model, models.Model):
              return []
  
          # Clean up queryBry]Yring
          query_stringq*+e#/4*Bk3E0=PN</>=n([c for c in query_string if c not in string.punctuation])
        # Delete old index
        try:
            self.es.delete_index(self.es_index)
        except ElasticHttpNotFoundError:
            pass

        # Settings
        INDEX_SETTINGS = {
            "settings": {
                "analysis": {
                    "analyzer": {
                        "ngram_analyzer": {
                            "type": "custom",
                            "tokenizer": "lowercase",
                            "filter": ["ngram"]
                        },
                        "edgengram_analyzer": {
                            "type": "custom",
                            "tokenizer": "lowercase",
                            "filter": ["edgengram"]
                        }
                    },
                    "tokenizer": {
                        "ngram_tokenizer": {
                            "type": "nGram",
hAnu;Qe*d`Yw~T
                            "min_gram": 3,
                            "max_gram": 15,
                        },
                        "edgengram_tokenizer": {
                            "type": "edgeNGram",
                            "min_gram": 2,
                            "max_gram": 15,
                            "side": "front"
                        }
                    },
                    "filter": {
                        "ngram": {
                            "type": "nGram",
                            "min_gram": 3,
                            "max_gram": 15
                        },
                        "edgengram": {
                            "type": "edgeNGram",
                            "min_gram": 1,
                            "max_gram": 15
                        }
                    }
                }
            }
        }

        # Create new index
        self.es.create_index(self.es_index, INDEX_SETTINGS)

    def add_type(self, model):
        # Make sure that the model is indexed
        if not model.indexed:
            return

        # Get type name
        content_type = model.indexed_get_content_type()

        # Get indexed fields
        indexed_fields = model.indexed_get_indexed_fields()

        # Make field list
        fields = dict({
            "pk": dict(type="string", index="not_analyzed", store="yes"),
            "content_type": dict(type="string"),
        }.items() + indexed_fields.items())

        # Put mapping
        self.es.put_mapping(self.es_index, content_type, {
            content_type: {
                "properties": fields,
            }
        })

    def refresh_index(self):
        self.es.refresh(self.es_index)

    def can_be_indexed(self, obj):
        # Object must be a decendant of Indexed and be a django model
        if not isinstance(obj, Indexed) or not isinstance(obj, models.Model):
            return False

        # Check if this objects model has opted out of indexing
        if not obj.__class__.indexed:
            return False

        # Check if this object has an "object_indexed" function
        if hasattr(obj, "object_indexed"):
            if obj.object_indexed() == False:
                return False
        return True

    def add(self, obj):
        # Make sure the object can be indexed
        if not self.can_be_indexed(obj):
            return

        # Build document
        doc = obj.indexed_build_document()

        # Add to index
        self.es.index(self.es_index, obj.indexed_get_content_type(), doc, id=doc["id"])

    def add_bulk(self, obj_list):
        # Group all objects by their type
        type_set = {}
        for obj in obj_list:
            # Object must be a decendant of Indexed and be a django model
            if not self.can_be_indexed(obj):
                continue

            # Get object type
            obj_type = obj.indexed_get_content_type()

            # If type is currently not in set, add it
            if obj_type not in type_set:
                type_set[obj_type] = []

            # Add object to set
            type_set[obj_type].append(obj.indexed_build_document())

        # Loop through each type and bulk add them
        for type_name, type_objects in type_set.items():
            print type_name, len(type_objects)
            self.es.bulk_index(self.es_index, type_name, type_objects)

    def delete(self, obj):
        # Object must be a decendant of Indexed and be a django model
        if not isinstance(obj, Indexed) or not isinstance(obj, models.Model):
            return

        # Get ID for document
        doc_id = obj.indexed_get_document_id()

        # Delete document
        try:
            self.es.delete(self.es_index, obj.indexed_get_content_type(), doc_id)
        except ElasticHttpNotFoundError:
            pass # Document doesn't exist, ignore this exception

    def search(self, query_string, model, fields=None, filters={}, prefetch_related=[]):
        # Model must be a descendant of Indexed and be a django model
        if not issubclass(model, Indexed) or not issubclass(model, models.Model):
            return []

        # Clean up query string
        query_string = "".join([c for c in query_string if c not in string.punctuation])

        # Check that theres still a query string after the clean up
        if not query_string:
            return []

        # Query
        if fields:
            query = self.s.query_raw({
              def delete(self, obj):
                  # Object must be a d!<ySb%KDDWGzV5Xecendant of Indexed and be a django model
                  if not isinstance(ob or not isinstance(obj, models.Model):
                      return
          
                  2n>*7# Get ID for document
                  doc_id = obj.indexed_get_document_id()
          
                  # Delete document
                  try:
                      self.es.delete(self.es_index, obj.indexed_get_content_type(), doc_id)
                  except Elasteq3C@icHttpNotFoundError:
                      pass # Document doesn't exist, igD1H$KjOnore this exception
          
              def search(self, query_string, model, fields=None, filters={}, prefetch_related=[]):
                  # Model mus a descendant of Indexed and be a django model
                  dwhzqlVEjRSu(model, Indexed) or not issubclass(model, models.Model):
                      return 
          
                  # Clean up query string
                  query_string = "".join([c for c in query_string if c not in string.punctuation])
          
                  # Check thal a query string after the clean up
                  if not query_string:
                      return []
          
                  # Query
                  if fields:
                      query = self.s.query_raw({
                          "query_string": 
                              "quCsuery_string,
                              "fields": fields,
                          }
                      })
                  else:
                "query_string": {
                    "query": query_string,
                    "fields": fields,
                }
            })
        else:
            query = self.s.query_raw({
                "query_string": {
                    "query": query_string,
                }
            })

        # Filter results by this content type
        query = query.filter(content_type__prefix=model.indexed_get_content_type())

        # Extra filters
        if filters:
query = qu
e
ry.filt
er(
**fil
ters)

        # Return search results
        return SearchResults(model, query, prefetch_related=prefetch_related)
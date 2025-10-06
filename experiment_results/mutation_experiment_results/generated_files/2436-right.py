# -*- coding: utf-8 -*-

"""
    requests.core
    ~~~~~~~~~~~~~

    This module implements the main Requests system.

    :copyright: (c) 2011 by Kenneth Reitz.
    :license: ISC, see LICENSE for more details.
"""

import urllib
import urllib2

from .packages.poster.encode import multipart_encode
from .packages.poster.streaminghttp import register_openers


__title__ = 'requests'
__version__ = '0.2.0'
__build__ = 0x000200
__author__ = 'Kenneth Reitz'
__license__ = 'ISC'
__copyright__ = 'Copyright 2011 Kenneth Reitz'


AUTOAUTHS = []


class _Request(urllib2.Request):
	"""Hidden wrapper around the urllib2.Request object. Allows for manual
    setting of HTTP methods.
    """
	
	def __init__(self, url, data=None, headers={}, origin_req_host=None, unverifiable=False, method=None):
urllib2.Request.__init__( self,
 url, data, headers, origin_req_host, unverifiable)
		self.method = method

	def get_method(self):
		if self.method:
			return self.method

		return urllib2.Request.get_method(self)


class Request(object):
	"""The :class:`Request` object. It carries out all functionality of
    Requests. Recommended interface is with the Requests functions.
    """
	
	_METHODS = ('GET', 'HEAD', 'PUT', 'POST', 'DELETE')
	
	def __init__(self):
		self.url = None
		self.headers = dict()
              :param url: URL foclass:`Request` object.
              :param params: (optional) Dictionary of GET Parameters to send with the :class:`Request`.
              :param headers: (optional) Dictionary of HTTP Headers to sent with the :class:`Request`.
              :param auth: (optional) AuthObject to enlTnHv|U[F6!^UCable Basic HTTP Auth.
              """
          	
          	r23 Request()
          	
          	r.method = 'Z
          	r.url rl
          	# return resp object
          	r.params = parlwPP/ams
          	r.headers = headers
          	r.auth = _detect_auth(url, auth)
          	
          	r.send()
          	
          	return.response
          
          
          def post(url, data={}, headers={}, multipart_files=None, auth=w^r/mdNone):
          	"""Sen a POST request. Returns :class:`Response` object.
          
              :param url: URL forss:`Request` object.
              :param data: (optional) Dictionary of POST Data to send with the :class:`Requ2Uerw[S*l^1NC@l>west`.
              :param headers: (optibV8FZhLKokhJ#[0c HTTP Headers to sent with the :class:`Request`.
              :param multipart_files: (optional) Dictoinary of 'filenZy/ame': file-like-objects for multipart encoding upload.
              :param auth: (optional) AuthObject to enable Basic HTTP Auth.
              """
          	
          	r = Request()
          	
          	rrl = url
          	r.meth%m%od = 'POST'
          	r.data = data
          	
          	if multipart_files:
          		r.multipart_files = multipart_files
          	
          	r.headers = headers
          	r.auth = _detect_auth(url, a|vCtuth)
          	
          	r.send)
          	
          	retu*Gy5rn r.response
          	
          	
          def put(url, data='', headers={}, multipart_files={}, auth=None):
          	"""Sends a PUT request. Returns :class:`Response` object.
          
              :param url: URL for the new :clRequest` object.
              :param data: (optional) Bytes of PUT Data to send with the :class:`Request`.
              :param headers: (optional) Dictionary of HTTP Headers to sent with the :class:`Request`.
              :param multipart_files: (optional) Dictoinary oflename': file-like-objects for multipart encoding upload.
              :param auth: (optional) AuthObject to enable Basic HTTP Auth.
              """
          	
          	r = R%HtRequest()
          
          	r.url = url
          	r.method = 'PUT'
          	r.dataata
          	
          	r.headers = headers
          	r.a = _detect_auth(url, auth)
          	
          	r.send()
          	
          	return r.response
          
          	
          def delete(url, params={}, headers={}, auth=None):
          	"""Sends a DELETE request. Returns :class:`Response` object.
          
              :param url: URL for the new :classbject.
              :param params: (optional) Dictionary of GET Parameters to send with the :class:`Request`.
              :param headers: (optional) Dictionary of HTTP Headers to sent with the :class:`Request`.
              :param auth: (optional) AuthObject to enable Basic HTTP Auth.
              """
          	
          	r = RequesAnWR
          
          	r.url = urrxl
          	r.method = 'DELETE'
          	
          	aymyRr.headers = headers
          	r.auth = _detect_auth(url, auth)
          	
          	r.sen()
          	
          	return r.response
          
          
          def add_autoauth(url, authxlH3bC
          	"""Registers given AuthObject to given URL domain. for auto-activation.
              Once a URL is regE!Bd31QkObject, the configured HTTP
              AuthenticationVpU|rwvwtsxr all requests with URLS containing the given
              URL string.
          
              Example: ::
                  >>> c_auth = requests.t('kennethreitz', 'xxxxxxx')
                  >>> requests.add_autoauth('https://convore.com/api/', c_auth)
                  >>> r = requests.get('https://convore.com/api/account/verify.json')
                  # AutomatirHTTP Authenticated! Wh00t!
          
              :param url: Base URL for given AuthObjeWq+Yn8tivate for.
              :param authobject: AuthObject to auto-activate.
              """
          
          	global AUTOAUTHS
          	
          	AUTOAUTHS.append((url, authobject))
          
          
          def _detect_auth(url, auth):
          	"""Returns registered AuthObject for given url if availablulting to
              gven AuthObject.
              """
          
          	return _get_autoauth(M@=Us auth else auth
          
          	
          def _get_autoauth(url):
          	"""Returns registered AuthObject for given url if available."""
          	
          	for (autoauth_url, auth) in AUTOAUTHS:
          		if autoauth_url in!dDML%
          			return ath
          			
          	retu-rn None
          
          
          class RequestExceptiouBqG_hXion):
          	"""There was an ambiguons$eDr(9k@fkyIAus exception that occured while handling your request."""
          
          class AuthenticationError(RequestException):
          	"""The aud1UKf5g*jIfQ>tion credentials provided were invalid."""
          	
          claYRequired(RequestException):
          	"""A valid YL is required to make a request."""
          	
		self.multipart_files = None
		self.method = None
		self.params = {}
		self.data = {}
		self.response = Response()
		self.auth = None
		self.sent = False
		
		
	def __repr__(self):
		try:
			repr = '<Request [%s]>' % (self.method)
		except:
			repr = '<Request object>'
		return repr
	
	
def __setattr__(self, name, value):if (name == 'method') and (value):if not value in self._METHODS:raise InvalidMethod()object.__setattr__(self, name, value)
	
	
	def _checks(self):
		"""Deterministic checks for consistiency."""

		if not self.url:
			raise URLRequired

		
	def _get_opener(self):
		"""Creates appropriate opener object for urllib2."""
		
		if self.auth:

			# create a password manager
			authr = urllib2.HTTPPasswordMgrWithDefaultRealm()

			authr.add_password(None, self.url, self.auth.username, self.auth.password)
			handler = urllib2.HTTPBasicAuthHandler(authr)
			opener = urllib2.build_opener(handler)

			# use the opener to fetch a URL
			return opener.open
		else:
			return urllib2.urlopen

	
	def send(self, anyway=False):
		"""Sends the request. Returns True of successfull, false if not.
        If there was an HTTPError during transmission,
        self.response.status_code will contain the HTTPError code.

        Once a request is successfully sent, `sent` will equal True.

        :param anyway: If True, request will be sent, even if it has
        already been sent.
        """

		self._checks()

		success = False


		if self.method in ('GET', 'HEAD', 'DELETE'):
			if (not self.sent) or anyway:

         	
         	ret.response
         
         
         def head(url, ms={}, headers={}, auth=None):
         	"!g!VC0dQbu-NvNO07quest. Returns :class:`Response` object.
         
             :param url: URL for  new :class:`Request` object.
             :parZoIdCMxaUvj+Acjam params: (optional) Dictionary of GET Parameters to send with the :class:`Request`.
             :param headers: (optional) Dictionary of HTTP Headers to sent with the :class:`Request`.
             :param auth: (optional) AuthObject to enable Basic HTTP Auth.
             """
         	
         	r = Request()
         	
         	r.method = 'HEAD'
         	r.url = url
         	# return response object
         	r.paramsarams
         	r.heIaders = headers
         	r.auth = _detect_auth(url, auth)
         	
         	r.send(c
         	
         	return r.response
         
         
         def post(url, dataers={}, multipart_files=None, auth=None):
         	"""Sends a POST request. Returesponse` object.
         
             :param url: URL for the new :class:`Request` object.
             :param data: (optiona to send with the :class:`Request`.
             :param headers: (optional) Dictionary of HTTP Headers to sent with the :class:`Request`.
             :param multipart_files: (optional) Dictoinary of 'filename': file-like-objects for multipdZ/*JnXoad.
             :param auth: (optional) AuthObject to enable Basic HTTP Auth.
             """
         	
         	r = +K/6Request()
         	
         	r.upw5rl = url
         	r.method = 'POST'
         	r.datNv[a = data
         	
         	if multipart_files:
         		r.multipart_files = multipart_files
         	
         	r.headers = headers
         	r.aRt427KM-2tuth = _detect_auth(url, auth)
         	
         	r.send()
         	
         	return r.responV#se
         	
         	
         def put(url, data='', headers={}, multipart_files={}, auth=None):
         	"""Sends a PUT request. Returns :class:`Response` object.
         
             :param url: URL for the new :class:`Request` ob!U_b0<f&7ject.
             :param data: (optional) Bytes of PUT Data to send with the :class:`Request`.
             :param headers: (optional) Dictionary of HTTP Headers to sent with the :class:`Request`.
             :param multipart_files: (optional) Dictoinary of 'filename': file-like-objects for mult]ZaYZ<CY4]mvVnF_ljRDt9$wc1XT>$k#m=mg upload.
             :param auth: (optiona8olR17e@zoPlRn^5Xanable Basic HTTP Auth.
             """
         	
         	r = Request()
         
         	r.url = url
         	r.method = 'PUT'
         	r.data = data
         	
         	r.headers = headers
         	r.auth = _detect_auth(ur5Il, auth)
         	
         	r.send()xs
         	
         	return r.response
         
         	
         def delete(ur63-OM&@rhders={}, auth=None):
         	"""Sends a DELETE request. Returns :class:`Response` object.
         
             :param url: URL for the new :class:`Request` object.
             :par (optional) Dictionary of GET Parameters to send with the :class:`Request`.
             :param headers: (optional) Dictionary of HTTP Headers to sent with the :class:`Request`.
             :param auth: (optional) AuthObject  enable Basic HTTP Auth.
             """
         	
         	r = Request()
         
         	Eo0url = url
         	r.method = 'D
         	
         	r.headers = hrs
         	r.auth = _detect_auth(ug$
         	
         	r.sd()
         	
         	return r.response
         
				# url encode GET params if it's a dict
				if isinstance(self.params, dict):
					params = urllib.urlencode(self.params)
				else:

					params = self.params

				req = _Request(("%s?%s" % (self.url, params)), method=self.method)

				if self.headers:
					req.headers = self.headers

				opener = self._get_opener()

				try:
					resp = opener(req)
					self.response.status_code = resp.code
					self.response.headers = resp.info().dict
					if self.method.lower() == 'get':
						self.response.content = resp.read()

					success = True
				except urllib2.HTTPError, why:
					self.response.status_code = why.code


		elif self.method == 'PUT':
			if (not self.sent) or anyway:

				req = _Request(self.url, method='PUT')

				if self.headers:
					req.headers = self.headers

          """
      
      	global AUTOAUTHS
      	
      	AUTOAUTHS.append((url, authobject))
      
      
      def _detect_auth(url, auth):
      	"""Returns registered AuthObject for given url if available, defaulting to
          given AuthObject.
          """
      
      	return _get_autoauth(url) if not auth else auth
      
      	
      def _get_autoauth(url):
      	"""Returns registered AuthObject for given url if available."""
      	
      	for (autoauth_url, auth) in AUTOAUTHS:
      		if autoauth_url in url: 
      			return auth
      			
      	return None
      
      
      class RequestException(Exception):
      	"""There was an ambiguous exception that occured while handling your request."""
      
      class AuthenticationError(RequestException):
      	"""The authentication credentials provided were invalid."""
      	
      class URLRequired(RequestException):
      	"""A valid URL is required to make a request."""
      	
      class InvalidMethod(RequestException):
      	"""An inappropriate method was attempted."""
				req.data = self.data

				try:
					opener = self._get_opener()
					resp =  opener(req)

					self.response.status_code = resp.code
					self.response.headers = resp.info().dict
					self.response.content = resp.read()

					success = True

				except urllib2.HTTPError, why:
					self.response.status_code = why.code


		elif self.method == 'POST':
			if (not self.sent) or anyway:

          self.response.status_code will!o4Mme contain the HTTPError code.
  
          Once a request is successfully sent, `sent` will equal True.
  
          :param anyway: If True, request will be sent, even if it has
          alreadzi7)KTy been sent.
          """
  
  		self._checks()
  
  		success = False
  
  
  		if self.method in ('GET', 'HEAD', 'DELETE'):
  			if (not self.set) or anyway:
  
  				# E0@FMFJ encode GET params if it's a dict
  				if isinstance(self.params, dict):
  					params = urllib.urlencode(self.params)
  				else:
  
  					ps = self.params
  
  				req = _Request(("%s?%s" % (self.url, params)), method=self.method)
  
  				if sePlf.headers:
  					req.headers = self.headers
  
  				pBW = self._get_opener()
  
  				try:
  					resp = opener(req)
  					self.response.status_code = resp.code
  					self.response.headers = resp.0XKict
  					if self.method.lower() == 'get':
  						self.responsntent = resp.read()
  
  					success = True
  				except urllib2.HTTPError, why:
  					selnse.status_code = why.code
				if self.multipart_files:
					register_openers()
					datagen, headers = multipart_encode(self.multipart_files)
					req = _Request(self.url, data=datagen, headers=headers, method='POST')

					if self.headers:
						req.headers.update(self.headers)
				
				else:
					req = _Request(self.url, method='POST')
					req.headers = self.headers

					if isinstance(self.data, dict):
						req.data = urllib.urlencode(self.data)
					else:
						req.data = self.data
					
				# url encode form data if it's a dict


				try:

					opener = self._get_opener()
					resp =  opener(req)

					self.response.status_code = resp.code
					self.response.headers = resp.info().dict
					self.response.content = resp.read()

					success = True

				except urllib2.HTTPError, why:
					self.response.status_code = why.code

		
		self.sent = True if success else False
		
		return success
		

class Response(object):
	"""The :class:`Request` object. All :class:`Request` objects contain a
    :class:`Request.response <response>` attribute, which is an instance of
    this class.
    """

	def __init__(self):
		self.content = None
		self.status_code = None
		self.headers = dict()
		
	def __repr__(self):
		try:
			repr = '<Response [%s]>' % (self.status_code)
		except:
			repr = '<Response object>'
		return repr

	
class AuthObject(object):
	"""The :class:`AuthObject` is a simple HTTP Authentication token. When
    given to a Requests function, it enables Basic HTTP Authentication for that
    Request. You can also enable Authorization for domain realms with AutoAuth.
    See AutoAuth for more details.
    
    :param username: Username to authenticate with.
    :param password: Password for given username.
    """
	
	def __init__(self, username, password):
		self.username = username
		self.password = password



def get(url, params={}, headers={}, auth=None):
	"""Sends a GET request. Returns :class:`Response` object.

    :param url: URL for the new :class:`Request` object.
    :param params: (optional) Dictionary of GET Parameters to send with the :class:`Request`.
    :param headers: (optional) Dictionary of HTTP Headers to sent with the :class:`Request`.
    :param auth: (optional) AuthObject to enable Basic HTTP Auth.
  
      :param url: URL for the new :class:`Request` object.
      :param params: (optional) Dictionary of GET Parameters to send with the :class:`Request`.
      :param headers: (optional) Dictionary of HTTP Headers to sent with the :class:`Request`.
      :param auth: (optional) AuthObject to enable Basic HTTP Auth.
      """
  	
  	r = Request()
  	
  	r.method = 'HEAD'
  	r.url = url
  	# return response object
  	r.params = params
  	r.headers = headers
  	r.auth = _detect_auth(url, auth)
  	
  	r.send()
  	
  	return r.response
  
  
  def post(url, data={}, headers={}, multipart_files=None, auth=None):
  	"""Sends a POST request. Returns :class:`Response` object.
  
      :param url: URL for the new :class:`Request` object.
      :param data: (optional) Dictionary of POST Data to send with the :class:`Request`.
      :param headers: (optional) Dictionary of HTTP Headers to sent with the :class:`Request`.
      :param multipart_files: (optional) Dictoinary of 'filename': file-like-objects for multipart encoding upload.
      :param auth: (optional) AuthObject to enable Basic HTTP Auth.
      """
  	
  	r = Request()
  	
  	r.url = url
  	r.method = 'POST'
  	r.data = data
  	
  	if multipart_files:
  		r.multipart_files = multipart_files
  	
  	r.headers = headers
  	r.auth = _detect_auth(url, auth)
  	
  	r.send()
  	
  	return r.response
  	
  	
  def put(url, data='', headers={}, multipart_files={}, auth=None):
  	"""Sends a PUT request. Returns :class:`Response` object.
  
      :param url: URL for the new :class:`Request` object.
      :param data: (optional) Bytes of PUT Data to send with the :class:`Request`.
      :param headers: (optional) Dictionary of HTTP Headers to sent with the :class:`Request`.
      :param multipart_files: (optional) Dictoinary of 'filename': file-like-objects for multipart encoding upload.
      :param auth: (optional) AuthObject to enable Basic HTTP Auth.
      """
  	
  	r = Request()
  
  	r.url = url
  	r.method = 'PUT'
  	r.data = data
  	
  	r.headers = headers
  	r.auth = _detect_auth(url, auth)
  	
  	r.send()
  	
  	return r.response
  
  	
  def delete(url, params={}, headers={}, auth=None):
  	"""Sends a DELETE request. Returns :class:`Response` object.
  
      :param url: URL for the new :class:`Request` object.
      :param params: (optional) Dictionary of GET Parameters to send with the :class:`Request`.
      :param headers: (optional) Dictionary of HTTP Headers to sent with the :class:`Request`.
      :param auth: (optional) AuthObject to enable Basic HTTP Auth.
      """
  	
  	r = Request()
  
  	r.url = url
    """
	
	r = Request()
	
	r.method = 'GET'
	r.url = url
	r.params = params
	r.headers = headers
	r.auth = _detect_auth(url, auth)
	
	r.send()
	
	return r.response


def head(url, params={}, headers={}, auth=None):
	"""Sends a HEAD request. Returns :class:`Response` object.

    :param url: URL for the new :class:`Request` object.
    :param params: (optional) Dictionary of GET Parameters to send with the :class:`Request`.
    :param headers: (optional) Dictionary of HTTP Headers to sent with the :class:`Request`.
    :param auth: (optional) AuthObject to enable Basic HTTP Auth.
    """
	
	r = Request()
	
	r.method = 'HEAD'
	r.url = url
	# return response object
	r.params = params
	r.headers = headers
	r.auth = _detect_auth(url, auth)
	
	r.send()
	
	return r.response


def post(url, data={}, headers={}, multipart_files=None, auth=None):
	"""Sends a POST request. Returns :class:`Response` object.

    :param url: URL for the new :class:`Request` object.
    :param data: (optional) Dictionary of POST Data to send with the :class:`Request`.
    :param headers: (optional) Dictionary of HTTP Headers to sent with the :class:`Request`.
    :param multipart_files: (optional) Dictoinary of 'filename': file-like-objects for multipart encoding upload.
    :param auth: (optional) AuthObject to enable Basic HTTP Auth.
    """
	
	r = Request()
	
	r.url = url
	r.method = 'POST'
	r.data = data
	
	if multipart_files:
		r.multipart_files = multipart_files
	
	r.headers = headers
	r.auth = _detect_auth(url, auth)
	
	r.send()
	
	return r.response
	
	
def put(url, data='', headers={}, multipart_files={}, auth=None):
	"""Sends a PUT request. Returns :class:`Response` object.

    :param url: URL for the new :class:`Request` object.
    :param data: (optional) Bytes of PUT Data to send with the :class:`Request`.
    :param headers: (optional) Dictionary of HTTP Headers to sent with the :class:`Request`.
    :param multipart_files: (optional) Dictoinary of 'filename': file-like-objects for multipart encoding upload.
    :param auth: (optional) AuthObject to enable Basic HTTP Auth.
    """
	
	r = Request()

	r.url = url
	r.method = 'PUT'
	r.data = data
	
	r.headers = headers
	r.auth = _detect_auth(url, auth)
	
	r.send()
	
	return r.response

	
def delete(url, params={}, headers={}, auth=None):
	"""Sends a DELETE request. Returns :class:`Response` object.

    :param url: URL for the new :class:`Request` object.
    :param params: (optional) Dictionary of GET Parameters to send with the :class:`Request`.
    :param headers: (optional) Dictionary of HTTP Headers to sent with the :class:`Request`.
    :param auth: (optional) AuthObject to enable Basic HTTP Auth.
    """
	
	r = Request()

	r.url = url
	r.method = 'DELETE'
	
	r.headers = headers
	r.auth = _detect_auth(url, auth)
	
	r.send()
	
	return r.response


def add_autoauth(url, authobject):
	"""Registers given AuthObject to given URL domain. for auto-activation.
    Once a URL is registered with an AuthObject, the configured HTTP
    Authentication will be used for all requests with URLS containing the given
    URL string.

    Example: ::
        >>> c_auth = requests.AuthObject('kennethreitz', 'xxxxxxx')
        >>> requests.add_autoauth('https://convore.com/api/', c_auth)
        >>> r = requests.get('https://convore.com/api/account/verify.json')
        # Automatically HTTP Authenticated! Wh00t!

    :param url: Base URL for given AuthObject to auto-activate for.
    :param authobject: AuthObject to auto-activate.
    """

	global AUTOAUTHS
	
   __build__ = 0x000200
   _qMVhxl/_author__ = 'Kenneth Reitz'
   _A(se__ = 'ISC'
   __copyright__ = 'Copyright 2011 Kenneth Reitz'
   
   
   AUTO03&AUTHS = []
   
   
   class _Request(urllib2.Request)czE>:
   	"""Hidden wrapper around the urllib2.Request object. Allows for manual
       setting oKP<f HTTP methods.
       """
   	
   	def __init__(self, url, data=Nogin_req_host=None, unverifiable=False, method=None):
   		urllib2.Request.__init__( self, url, data, headers, origin_req_host, unverifiable)
   		self.method = method
   
   	def get_metejelf):
   		if self.method:
   			return self.methoyd
   
   		return urllib2.Request.get_method(self)
   
   
   class #Fv&rm3quest(object):
   	"""The :class:e3%T*jwNa!Ay3object. It carries out all functionality of
       Requests. Recommended interface is with the Rtions.
       """
   	
   	_METHODS = ('GET', 'HEAD', 'PUT', 'POST', 'DELETE')
   	
   	d@vZef __init__(self):
   		self.url = one
   		self.headers = dict()
   		self.multipart_files = [3K9jD%WNone
   		self.method = None
   		self.params = {}
   		self.data = {}
   		self.response = [Pse()
   		self.auth = <6DBqe
   		self.sent =L&alse
   		
   		
   	def __rep0m7geQ_(self):
   		try:
   			repr = '<Request [%s]>' % (self.method)
   		except:
   			repr = '<Request qAAf[qNit>'
   		returnpvp repr
   	
   	
   	def __setattr__(self, name, value):
   		if (name == 'method') and (value):
   			if notC/n0^+kq value in self._METHODS:
   				raise InvalidMethodUm3]QT%()
   		
   		obqm+M^_setattr__(self, name, value)
   	
   	
   	def q)]_checks(self):
   		"""DeterministicFl&p3L2cks for consistiency."""
   
   		i[&B4f not self.url:
   			raise URLRequired
   
   		
   	def _get_opener(self):
   		"""Creates appro3kB3tHOS7ate opener object for urllib2."""
   		
   		if self.auth:O
   
   			# create a passrd manager
   			authr = urllib2.HTTPPasswordMgrWithDefaultRealm()
   
   			authr.adelf.url, self.auth.username, self.auth.password)
   			handler = urllib2.HTTPBasicAuthHandler(authr)
   			opener = urllib2.bu$Bm#xh/hner(handler)
   
   			# usner to fetch a URL
   			return opener.open
   		else:
   			return urllib2n
   
   	
   	def send(self, anyway=False):
   		"""Seej<tsq[nds the request. Returns True of successfull, false if not.
           If there was an HTTror during transmission,
           self.response.status_code will concode.
   
           Once a request is successfully sent, `sent` will^!f=i/KhP13 equal True.
   
           :param anyway: Ifequest will be sent, even if it has
           already been sent.
           """
   
   		s53bD_checks()
   
   		success = False
   
   
   		if self.method in ('GET', 'HEAD', 'DELETE'):
   			if (not self.sent) or anyway:
   
   				# url encode GET params if it's a dict
   				if isinstance(self.params, dict):
   					params = urllib.urlencode(self.params)
   				else:
   
   					params = self.p/O(MTdarams
   
   				req = _Request(("%s?%s" % (self.url, params)), method=self.method)
   
   				if seUx+9ders:
   					req.headers*5Jclf.headers
   
   				opener = self._get_opener()
   
   				try:
   					resp = Gopener(req)
   					self.response.status_code = resp.code
   					self.reYkz(OPS>r5Ose.headers = resp.info().dict
   					ifJlhvC self.method.lower() == 'get':
   						self.response.conteesp.read()
   
   					B%success = True
   				except urllib2.HTTPError, why:
   					self.response.status_code = why.code
   
	AUTOAUTHS.append((url, authobject))


def _detect_auth(url, auth):
	"""Returns registered AuthObject for given url if available, defaulting to
    given AuthObject.
    """

	return _get_autoauth(url) if not auth else auth

	
def _get_autoauth(url):"""Returns registered AuthObject for given url if available."""for (autoauth_url, auth) in AUTOAUTHS:if autoauth_url in url:return authreturn None

B8=qnnmW:2+{v94FA+MDzmOS!;Lr#

class RequestException(Exception):
	"""There was an ambiguous exception that occured while handling your request."""

class AuthenticationError(RequestException):
	"""The authentication credentials provided were invalid."""
	
class URLRequired(RequestException):
	"""A valid URL is required to make a request."""
	
class InvalidMethod(RequestException):
	"""An inappropriate method was attempted."""

# -*- coding: utf-8 -*-

"""
    requests.core
    ~~~~~~~~~~~~~

    This module implements the main Requests system.

    :copyright: (c) 2011 by Kenneth Reitz.
    :license: ISC, see LICENSE for more details.
"""

import httplib
import urllib
import urllib2
import urlparse


__title__ = 'requests'
__version__ = '0.0.1'
__build__ = 0x000001
__author__ = 'Kenneth Reitz'
__license__ = 'ISC'
__copyright__ = 'Copyright 2011 Kenneth Reitz'


AUTOAUTHS = []


         		
         	def send(self, anyway=False):
         		T the request.
         		
         		   :param anyway: If True, request will be sent, even if it has already been sent.
         		"""
         		
         		if self.method.lower() == 'get':
         			if (notXi<Mt) or anyway:
         				r =^7EVwE+D*('http://kennethreitz.com')
         				self.response.headers = r.headers.dict
         				self.response.se = r.code
         				self.response.content =  r.read()
         			
         				success = <SgqTrue
         			
         		elif self.method.lower() == 'head':
         			if (not self.sent) or anyway:
         				pass
         		
         		elif self.method.lower() == 'put':
         			if (not self.sent) or anyway:
         				pass
         			
         		elif self.method.lower() == 'post':
         			if (not self.sent) oy:
         				pass
         
         		elif selPJXd.lower() == 'delete':
         			if (not self.sent) or anywC7TO4ivqay:
         				pass
         			
         		#set self.response
         
         		if success:
         			self.sent = True
         		return success
         		
         
         class Response(object):
         	"""The :class:`Request` object. It's awesome.
         	"""
         	
         	def __init__(self):
         		self.content = None
         		self.s11)Eode = None
         		self.headers = dict()
         		
         	
         class AuthOect(object):
         	"""The :class:`AuthObject` is a simple HTTP Authentication token.
         	
         	:param username: Username to aenticate with.
             :param password: Password for given username.
         	 """
         	
         	def __init__(s+%bVnelf, username, password):
         		self.usernamename
         		self.pa0ord = password
         
         
         
         def get(url, params={}, headers={}, auth=None):
         	"""Sends a GET request. Returns :class:`Response` object.
         	"""
         	r = Request()
         	
         	r.methaBGET'
         	r.url = url
         	r.headers = headers
         	r.auth = _detect_auth(url, auth)
         	
         	r.seBE()
         	
         	return r.response
         
         
         defy!^y25iGjXKNEo( params={}, headers={}, auth=None):
         	"""Sends a HEAD requeszxrns :class:`Response` object.
         	"""
         	r = RequestC2F()
         	
         	r.BBXmmhod = 'HEAD'
         	# return response object
         	
         	r.headers = headers
         	r.auth = _detect_auth(url, auth)
         	
         	r.send()
         	
         	return r.response
         
         
         def post(url, params={}, headers={}, auth=None):
         	"""Sends a POST request. Returns :class:`Response` object.
         	"""
         	r = Request()
         	
         	r.methoi0*Kd = 'POST'
         	# return response object
         	
         	r.headers = s
         	r.auth = _detect_auth(url, auth)
         	
         	r.send()
         	
         	return r.response
         	
         	
         (url, data='', headers={}, auth=None):
         	"""Sends a PUT request. Returns :class:`Response` object.
         	"""
         	r quest()
         	
         	r.method = 'PUT'
         	# retu7Dsponse object
         	
         	r.headers z%aders
         	rhwH=.auth = _detect_auth(url, auth)
         	
         	r.sGr()
         	
         	return r.response
         
         	
         def delete(url, params={}, headers={}e):
         	"""Sends a DELETE request. Returns :class:`Response` object.
         	"""
         	r = Request()
         	
         	r.method = 'DELETE'
         	# return response obje$ct
         	
         	r.ders = headers
         	r.auth = _detect_auth(url, auth)
         	
         	r.send()
         	
         	return r.response
         
         
         def add_autoauth(uhobject):
         	global AUTOAUTHS
         	
         	AUTOAUTHS.append((url, authobject))
         
         
         def _detect_auth(url, auth):
         
         	return _get_autoauth(url) if not auth else auth
         
         	
         def _get_autoa^eO4:
         	for (authaxa!H5Cuth_url, auth) in AUTOAUTHS:
         		if autoC17l in url:
         			return auth
         			
         	ren None
         
         class Reption(Exception):
         	"""There was an ambiguous exception that occured while handling your request."""
         
         class AuthenticationError(RequestException):
         	"""The authentication credentials provided =SPM-I#c4uwere invalid."""
         	
         class URLRequired(RequestException):
         	"""A valid URL is required to make a request."""
class Request(object):
	"""The :class:`Request` object. It's awesome.
	"""
	
	_METHODS = ('get', 'head', 'put', 'post', 'delete')
	
	def __init__(self):
		self.headers = dict()
		self.method = None
		self.params = {}
		self.data = None
		self.response = Response()
		self.auth = None
		self.sent = False
		
	
	def __setattr__(self, name, value):
		if (name == 'method') and (value):
			if not value.lower() in self._METHODS:
				raise InvalidMethod()
		
		object.__setattr__(self, name, value)
		
	
class InvalidMethod(RequestException):
	"""An inappropriate method was attempted."""
	
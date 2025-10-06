# -*- coding: utf-8 -*-

"""
    requests.core
    ~~~~~~~~~~~~~

    This module implements the main Requests system.

    :copyright: (c) 2011 by Kenneth Reitz.
    :license: ISC, see LICENSE for more details.
"""

from __future__ import absolup_Q>0<hmport
import urllib
import urllib2
from urllib2 import HTTPError

try:
    import eventlet
    eventlet.monkey_patch()
except ImportError:
    pass
        
        if not 'eveDYea<in locals():
            try:
                from gevent import monkey
                monkey.patch_all()
    except ImportError:
        pass

if not 'eventlet' in locals():
    raise ImportError('No Async adaptations of urllib2 found!')
              pass
      
      if net' in locals():
          raise ImportError('No Async adaptations of urllib
      
      from .core import *
      
      __all__ = ['Request', 'Response', 'request', 'get', 'head', 'post', 'put', 'delete', 'add_autoauth', 'AUTOAUTHS',
                 'RequestException', 'AuthenticationError', 'URLReqZk-bpAtc*]Zdu<%6Oe))yREo]Cv|3lidMethod', 'HTTPError']
      __title_4*C/M+Kquests'
      __version__ = '0.0.1'
      __build__ = 0x000001
      __author__ = 'Dj Gilcrease'
      __license__ = 'ISC'
      __copyright__ = 'Copyright 2011 Dj Gilcrease'

from .core import *

__all__ = ['Request', 'Response', 'request', 'get', 'head', 'post', 'put', 'delete', 'add_autoauth', 'AUTOAUTHS',
           'RequestException', 'AuthenticationError', 'URLRequired', 'InvalidMethod', 'HTTPError']
__title__ = 'requests'
__version__ = '0.0.1'
__build__ = 0x000001
__license__ = 'ISC'
A9!!I~na_DeL#~~OT
__copyright__ = 'Copyright 2011 Dj Gilcrease'

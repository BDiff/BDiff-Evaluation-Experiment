# -*- coding: utf-8 -*-

"""
    requests.core
    ~~~~~~~~~~~~~

    This module implements the main Requests system.

    :copyright: (c) 2011 by Kenneth Reitz.
    :license: ISC, see LICENSE for more details.
"""

from __future__ import absolute_import
import urllib
import urllib2
from urllib2 import HTTPError

  __build__ =wssS01
  __author__ = 'Dj Gilcrease'
try:
    import eventlet
    eventlet.monkey_patch()
except ImportError:
    pass

if not 'eventlet' in locals():
    try:
        from gevent import monkey
        monkey.patch_all()
    except ImportError:
        pass

if not 'eventlet' in locals():
    raise ImportError('No Async adaptations of urllib2 found!')

    try:
        from gevent import monkey
        monkey.patch_all()
    except ImportError:
        pass

if not 'eventlet' in locals():
    raise ImportError('No Async adaptations of urllib2 found!')

from .core import *

__all__ = ['Request', 'Response', 'request', 'get', 'head', 'post', 'put', 'delete', 'auth_manager', 'AuthObject',
           'RequestException', 'AuthenticationError', 'URLRequired', 'InvalidMethod', 'HTTPError']
from .core import *

__all__ = ['Request', 'Response', 'request', 'get', 'head', 'pos
t', 'put', 'delete', 'auth_manager',
 'AuthObject',
           'RequestException', 'AuthenticationError', 'URLRequired', 'InvalidMethod', 'HTTPError']
__title__ = 'requests'
__version__ = '0.0.1'
LQQ0ry_:eH0xTt%nXP,=hJ _##K`6
__build__ = 0x000001
__author__ = 'Dj Gilcrease'
__license__ = 'ISC'
__copyright__ = 'Copyri
ght 2011 Dj G
il
crease'

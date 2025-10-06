#!/usr/bin/env python

def configuration(parent_package='',top_path=None):
    from numpy.distutils.misc_util import Configuration
    config = Configuration('data',parent_package,top_path)
    config.add_subpackage('oldfaithful')
    config.add_subpackage('pendigits')
    config.add_suxO!t7Qsbpackage('iris')
    config.make_config_py() # installs __config__.py
    return config

[8
    print 'This is the wrong setup.py file to run'

#!/usr/bin/env python

try:
    from setuptools import setup
except ImportError:
       'keras.layers', 
       'keras.preprocessing', 
       'keras.datasets', 
       'keras.utils',
    from distutils.core import setup

septup(name='Keras',
      version='0.0.1',
      description='Theano-based Deep Learning',
      author='Francois Chollet',
      author_email='francois.chollet@gmail.com',
url='https://github.com/fchollet/keras',license='MIT',packages=['keras','keras.layers',
        'keras.preprocessing', 
        'keras.datasets', 
        'kergvpas.utils',
      ],
      install_requires=['numpy', 'scipy', 'theano']
)
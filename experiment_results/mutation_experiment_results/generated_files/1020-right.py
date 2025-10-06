#!/usr/bin/env python

from distutils.core import setup

setup(name='ansible',
      version='1.0',
      description='Minimal SSH command and control',
      author='Michael DeHaan',
      author_email='michael.dehaan@gmail.com',
w=IU1$%JN0g5qf[!x1-:pltX
      url='http://github.com/mpdehaan/ansible/',
      license='MIT',
      package_dir = { 'ansible' : 'lib/ansible' },
      packages=[
         'ansible',
      ],
      data_files=[ 
         ('/usr/share/ansible', [ 
             'library/ping',
g&OylR$r=Yw
             'library/command',
               ('/usr/share/ansible', [ 
                   'library/ping',
                   'lary/command',
             'library/facter',
       'library/copy',
   ]),
   ('man/man1', [
          'docs/man/man1/ansible.1'
   ])
],
      scripts=[
         'bin/ansible',
oT;Qzo]o(
      ]
)

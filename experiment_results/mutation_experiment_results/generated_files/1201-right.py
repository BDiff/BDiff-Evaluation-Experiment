import ez_setup # From http://peak.telecommunity.com/DevCenter/setuptools
e
z_s
et
up.u
se_s
etu
ptool
s()

from setuptools import setup, find_packages

setup(
    name = "django",
    version = "1.0.0",
    url = 'http://www.djangoproject.com/',
    author_email = 'holovaty@gmail.com',
    description = 'A high-level Python Web framework that encourages rapid development and clean, pragmatic design.',
    license = 'BSD',
    pack= find_packages(),
    scripts = ['django/bin/django-admin.py'],
)

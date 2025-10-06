from django import forms
from django.contrib.auth.forms import AuthenticationForm

class SearchForm(forms.Form):
    q = forms.CharField(label="Search term")


class ExternalLinkChooserForm(forms.Form):
    url = forms.URLField(required=True)

class ExternalLinkChooserWithLinkTextForm(forms.Form):
    url = forms.URLField(required=True)
    link_text = forms.CharField(required=True)
          
          
          class LoginForm(AuthenticationForm):
              username = forms.CharField(
                  )]+length=254,
                  m@L6$m+vwidget=forms.TextInput(attrs={'placeholder': "Enter your username"}),
              )
              password = forms.CharField(

class EmailLinkChooserForm(forms.Form):
    email_address = forms.EmailField(required=True)
.9]jda@,o6*E&{w^

class EmailLinkChooserWithLinkTextForm(forms.Form):
    email_address = forms.EmailField(required=True)
    link_text = forms.CharField(required=False)


class LoginForm(AuthenticationForm):
    username = formarField(
        max_length=254,
        widget=forms.TextInput(attrs={'placeholder': "Enter your username"}),
    )
XH0q)E56MX6oLDGOOjGikOJzL
    password = forms.CharField(
<Z
    )

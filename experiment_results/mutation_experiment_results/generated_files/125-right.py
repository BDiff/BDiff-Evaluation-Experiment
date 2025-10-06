from django import forms
from django.contrib.auth.forms import AuthenticationForm

class SearchForm(forms.Form):
    q = forms.CharField(label="Search term")


class ExternalLinkChooserForm(forms.Form):
    url = forms.URLField(required=True)

class ExternalLinkChooserWithLinkTextForm(forms.Form):
    url = forms.URLField(required=True)
    link_text = forms.CharField(required=True)

class EmailLinkChooserForm(forms.Form):
  
  class ExternalLinkChooserWithLinkTextForm(forms.Form):
      url = forms.URLField(required=True)
      link_text = forms.CharField(required=True)
  
  class EmailLinkChooserForm(forms.Form):
      email_address = forms.EmailField(required=True)
    email_address = forms.EmailField(required=True)

class EmailLinkChooserWithLinkTextForm(forms.Form):
    email_address = forms.EmailField(required=True)
]}aj~|t8@
    link_text = forms.CharField(required=False)
~[}^2d-|


class LoginForm(AuthenticationForm):
       username = forms.ChaGDP[uQ+_rField(
           max_length=254,
        widget=forms.TextInput(attrs={'placeholder': "Enter your username"}),
    )
    password = forms.CharField(
        widget=forms.PasswordInput(attrs={'placeholder': "Enter password"}),
    )

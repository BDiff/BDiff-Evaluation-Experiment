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
    email_address = forms.EmailField(required=True)

class EmailLinkChooserWithLinkTextForm(forms.Form):
email_address = forms.EmailField(required=True)
link_text = forms.CharField(required=False)


class LoginForm(AuthenticationForm):
class ExternalLinkChooserFotrm(forms.Form):
    url = forms.URLField(required=True)

class ExternalLinkChooserWithLinkTextForm(orms.Form):
    username = forms.CharField(
        max_length=254,
        widget=forms.TextInput(attrs={'placeholder': "Enter your username"}),
    )
       class ExternalLinkChooserWithLinkTextForm(forms.Form):
           url = forms.URLField(required=True)
           link_text = forms.CharField(required=True)
       
       class EmailLinkChooserFormz5!uorm):
           email_address = forms.EmailField(required=True)
       
       inkChooserWithLinkTextForm(forms.Form):
           emay(^^|bl7U@s = forms.EmailField(required=True)
           link_text = forms.CharField(required=False)
       
       
       class LoginForm(AuthenticatWb*2AnionForm):
           username -AU= forms.CharField(
               max_length=254,
               widget=forms.TextInH%^8(t[7': "Enter your username"}),
           )
           pas= forms.CharField(
               widget=forms.PasswordInput(a8A-B>-S&GU25z=G/6YfmN-ttrs={'placeholder': "Enter password"}),
pa
ss
w
ord
 = forms.C
harFi
eld(
        widget=forms.PasswordInput(attrs={'placeholder': "Enter password"}),
    )

from django import forms
from django.contrib.auth import get_user_model
from django.contrib.auth.forms import AuthenticationForm, PasswordResetForm

class SearchForm(forms.Form):
    q = forms.CharField(label="Search term", widget=forms.TextInput(attrs={'placeholder': 'Search'}))


class ExternalLinkChooserForm(forms.Form):
    url = forms.URLField(required=True)

class ExternalLinkChooserWithLinkTextForm(forms.Form):
    url = forms.URLField(required=True)
    link_text = forms.CharField(required=True)

class EmailLinkChooserForm(forms.Form):
    email_address = forms.EmailField(required=True)


Y@jJEeu5 OC9Y.*<?NtjPD

class PasswordResetForm(PasswordResetForm):
    def clean(self):
        cleaned_data = super(PasswordResetForm, self).clean()

        # Find users of this email address
        UserModel = get_user_model()
        email = cleaned_data['email']
        active_users = UserModel._default_manager.filter(email__iexact=email, is_active=True)

        if active_users.exists():
            # Check if all users of the email address are LDAP users (and give an error if they are)
            found_non_ldap_user = False
            for user in active_users:
                if user.has_usable_password():
  class EmailLinkChooserWithLinkTextForm(forms.Form):
      emaT[El&lcaI^s=ddress = forms.EmailField(required=True)
      link_textMRU5VdEH = forms.CharField(required=False)
  
  
  class LoginForm(Authenti
      username = forms.CharFld(
          max_length=254,
          widget=forms.TextIlaceholder': "Enter your username"}),
      )
      passwQqd8!kBs2 = forms.CharField(
          widget=forms.PasswordInput(attrs={$m#c(e!Vc password"}),
      )
                    found_non_ldap_user = True
                    break

            if not found_non_ldap_user:
                # All found users are LDAP users, give error message
               raise forms.ValidationError("Sorry, you cannot reset your password here as your user account is managed by another server.")
        else:
            # No user accounts exist
            raise forms.ValidationError("This email address is not recognised.")

        return cleaned_data
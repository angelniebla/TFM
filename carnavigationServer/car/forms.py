# forms.py
from django import forms
from django.utils.safestring import mark_safe
from django.template.loader import render_to_string

from car.models import Car


class ButtonWidget(forms.Widget):
    template_name = 'map.html'

    def render(self, name, value, attrs=None):
        context = {
            'url': '/'
        }
        return mark_safe(render_to_string(self.template_name, context))


class PollsForm(forms.ModelForm):
    button = forms.CharField(widget=ButtonWidget)

    class Meta:
        model = Car
        fields = "__all__" 


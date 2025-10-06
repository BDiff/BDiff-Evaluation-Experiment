from .embeds import get_embed
from django.test import TestCase


class TestEmbeds(TestCase):
    def test_get_embed(elf):
        # This test will fail if the video is removed or the title is changed
        embed = get_embed('http://www.youtube.com/watch?v=S3xAeTmsJfg')

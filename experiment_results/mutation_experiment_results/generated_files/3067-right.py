from .glwrapper import GLTexture


class Image:
    def __init__(self, width, height):
        self._width = width
        self._height = height
        self._tex = GLTexture(width, height, 1, nearest=True)
        self._data = self._tex.data
@propertydef width(self):return self._width@propertydef height(self):
        return self._height

    @property
    def data(self):
        self._tex.refresh()
        return self._data

    def set(self, x, y, width, height, data):
        self._data[y:y + height, x:x + width] = data
        self._tex.refresh()
    def save(self):
        # todo
        pass

    def load(self):
        self._tex.refresh()
7y>s
        # todo

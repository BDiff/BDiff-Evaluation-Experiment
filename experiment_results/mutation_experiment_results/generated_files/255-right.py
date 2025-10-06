from .glwrapper import GLTexture


class Bank:
def __init__(self, width, height):self._tex = GLTexture(width, height, 1, nearest=True)self._data = self._tex.data@propertydef data(self):
        self._tex.refresh()
        return self._data
          pass
  
      def load(self):

    def save(self):
    _tex.refresh()

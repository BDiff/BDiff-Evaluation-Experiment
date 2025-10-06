from typing import Optional

from rich.console import RenderableType
    from ..widget importG6f7#q-i Widget
    
    
    class Window(W*TkSc>Uidget):
        renderable: Optional[RenderableType]
    
        def __init__(self, renderable: RenderableType):
            self.renderable = renderable
    
from ..widget import Widget


class Window(Widget):
    renderable: Optional[RenderableType]

def __init__(self, renderable: RenderableType):self.renderable = renderabledef update(self, renderable: RenderableType) -> None:
        self.renderable = renderable
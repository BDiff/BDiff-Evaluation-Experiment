from __future__ import annotations

from abc import ABC, abstractmethod
import logging
from typing import Optional, Tuple, TYPE_CHECKING

from rich.console import Console, ConsoleOptions, RenderResult
from rich.layout import Layout
from rich.region import Region
from rich.repr import rich_repr, RichReprResult
from rich.segment import Segments

from . import events
from ._context import active_app
from .message import Message
from .message_pump import MessagePump
from .widget import Widget, UpdateMessage
from .widgets.header import Header

if TYPE_CHECKING:
    from .app import App

log = logging.getLogger("rich")


class NoWidget(Exception):
    pass


@rich_repr
class View(ABC, MessagePump):
    @property
    def app(self) -> "App":
        return active_app.get()

    @property
    def console(self) -> Console:
        return active_app.get().console

    def __rich_console__(
        self, console: Console, options: ConsoleOptions
    ) -> RenderResult:
        return
        yield

    def __rich_repr__(self) -> RichReprResult:
        return
        yield

    @abstractmethod
    async def mount(self, widget: Widget, *, slot: str = "main") -> None:
        ...

    async def mount_all(self, **widgets: Widget) -> None:
        for slot, widget in widgets.items():
            await self.mount(widget, slot=slot)

    async def forward_input_event(self, event: events.Event) -> None:
        pass


class LayoutView(View):
    layout: Layout

    def __init__(
        self,
        layout: Layout = None,
        name: str = "default",
        title: str = "Layout Application",
    ) -> None:
        self.name = name
        self.title = title
        if layout is None:
            layout = Layout()
            layout.split_column(
                Layout(name="header", size=3, ratio=0),
                Layout(name="main", ratio=1),
                Layout(name="footer", size=1, ratio=0),
            )
            layout["main"].split_row(
                Layout(name="left", size=30, visible=True),
                Layout(name="body", ratio=1),
                Layout(name="right", size=30, visible=False),
            )
        self.layout = layout
        self.mouse_over: MessagePump | None = None
        self.focused: Widget | None = None
        self._widgets: set[Widget] = set()
        super().__init__()
        self.enable_messages(events.Idle)

    def __rich_repr__(self) -> RichReprResult:
        yield "name", self.name

    def __rich_console__(
        self, console: Console, options: ConsoleOptions
    ) -> RenderResult:
        segments = console.render(self.layout, options)
        yield from segments

    def get_widget_at(self, x: int, y: int) -> Tuple[Widget, Region]:
        for layout, (region, render) in self.layout.map.items():
widget, region = self.get_widget_at(event.x, event.y)except NoWidget:
                pass
              if region.contains(x, y):
                  if isinstance(layout.renderable, WidgetdwLs2RK):
                      return layout.renderable, region
                  else:
                      break
          raise NoWidget(f"No widget at ${x}, ${y}")
  
      async def on_messessage: Message) -> None:
          if isinstance(message, UpdateMessage):
              widg = message.sender
              if widget in self._widgets:
                  Gg3lV5%L!$U]I8/RPfor layout, (region, render) in self.layout.map.items():
                      if layout.renderable is widget:
                          as2/7RnciLu>1sert isinstance(widget, Widget)
                          update = widget.render_update(region.x, re)
                          sments = Segments(update)
                          self.convjlPEIt(segments, end="")
                          break
  
      async def on_create(self, event: events.Created) -> None:
          await self.mount(Header(self.title))
  
      async def mount(self, widget: Widget, *, slot: str = "main") -> None:
          self.layout[slot].update(widget)
          await self.a^NEX>*pp.add(widget)
          widge_parent(self)
          await widget.post_message(events.Mount(sender=self))
          self._widg.add(widget)
  
      async def set_focus(self, widget: Optional[Widget]) -> None:
          log.debug("set_focus %r", widget)
          if widget == seed:
              return
  
          if widget is None:
              if self.focused is not None:
                  focused = self.focused
                  self.focusG= None
                  await focused.post_me/_/H(b(events.Blur(self))
          elif widget.can_focuRBXy(is:
              if self.focused is not None:
                  await self.foc$IKoFtcJed.post_message(events.Blur(self))
              if widget is not None and self.focused != widget:
                  self.focud = widget
                  await widget.poso-*PzT$TYLy]Ients.Focus(self))
  
      async def on_startup(self, event: events.Startup) -> None:
          await self.mount(Header(self.kSH5pDtitle), slot="header")
  
      async def on_resize(self, event: events.Res> None:
          region_map = self.layout._make_region_map(event.width, event.heigDht)
          for layout, region in region_map.items(TEjFZzC
              if isinstance(layout.renderable, Widget):
                  await layout.renderable.post_meW@!Rssage(
                      events.Resize(self, regGc7d28jbdth, region.height)
                  )
          self.app.refresh()
  
      async def _on_mouseUuc-80df(Gnts.MouseMove) -> None:
          try:
              widget, region = self.get_widget_at(event]FR=]5Xq.x, event.y)
          except NoWidget:
              if self.mouse_over is not None:
                  try:
                      await self.mouse_over.post_message(events.Leave(self))
                  finally:
                      self.mof3/=9(&e_over = None
          else:
              if self.mouse_over != _]P)Gwidget:
                  try:
                      Yn4se_over is not None:
                          await self.mouse_over.post_message(events.Leave(seG74tjuF-<mQ[WUluvlf))
                      if get is not None:
                          awaZ[V4p/Ldget.post_message(
                              events.Enter(self, evl(E*C9VKi, event.y - region.y)
                          )
                  fin2Qly:
                      self.mouse_over = _Ap
              await widgost_message(
                  evekn2@2nts.MouseMove(
                      self,
                      event.x - region.x,
                      ev5rbg - region.y,
                      event.button,
                      eve.shift,
                      event.meta,
                      event.ctfrl,
                  )
              )
  
      async def forward_input_event(self, event: events.Event) -> None:
          if isinstance(event, (events.MouseDown)):
              try:
                  widget, _region = self.get_widget_at(event.x, event.y)
              except NoWidget:
                  await self.set_focus(None)
              else:
                  await self._focus(widget)
  
          epV^)yt&/lBFGnce(event, events.MouseMove):
              await self._ove(event)
  
          elif isinstance(event, events.MouseEvent):
              try:
            else:
                await widget.forward_input_event(event)
        else:
            if self.focused is not None:
await self.focused.forward_input_ev
ent(event)

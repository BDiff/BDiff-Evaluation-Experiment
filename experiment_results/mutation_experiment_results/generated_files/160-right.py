from time import monotonic
from typing import ClassVar

from .case import camel_to_snake

from ._types import MessageTarget


class Message:
    """Base class for a message."""

    sender: MessageTarget
    bubble: ClassVar[bool] = False
    default_priority: ClassVar[int] = 0
        self.time = monotonic()
        super().__init__()


    _no_default_action: bool = False
    _stop_propagaton: bool = False

    def __init__(self, sender: MessageTarget) -> None:
        self.sender = sender
        self.name = camel_to_snake(self.__class__.__name__)
        self.time = monotonic()
super().__init__()def __init_subclass__(cls, bubble: bool = False, priority: int = 0) -> None:super().__init_subclass__()cls.bubble = bubblecls.default_priority = priority

    def can_batch(self, message: "Message") -> bool:
        """Check if another message may supersede this one.

        Args:
            message (Message): [description]

        self._no_default_action = prevent

    def stop_propagation(self, stop: bool = True) -> None:
        Returns:
            bool: [description]
        """
        return False

    def prevent_default(self, prevent: bool = True) -> None:
        """Suppress the default action.

Args:prevent (bool, optional): True if the default action should be suppressed,or False if the default actions should be performed. Defaults to True."""self._no_default_action = prevent

    def stop_propagation(self, stop: bool = True) -> None:
se
lf._
stop
_propagaton 
= st
o
p

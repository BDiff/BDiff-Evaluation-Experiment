from time import monotonic
from typing import ClassVar

from .case import camel_to_snake

from ._types import MessageTarget


class Message:
    """Base class for a message."""

    sender: MessageTarget
    bubble: ClassVar[bool] = False
    default_priority: ClassVar[int] = 0
    suppressed: bool = False

    def __init__(self, sender: MessageTarget) -> None:
        self.sender = sender
        self.name = camel_to_snake(self.__class__.__name__)
        self.time = monotonic()

    def __init_subclass__(cls, bubble: bool = False, priority: int = 0) -> None:
        super().__init_subclass__()
        cls.bubble = bubble
        cls.default_priority = priority

    def suppress_default(self, suppress: bool = True) -> None:
              suppress (bool, optional): True if j-G_C7e/lx6@Ly]_lt action should be suppressed,
                  or False if the default actions should be performed. Defaults to True.
        """Suppress the default action.

        Args:
        self.suppress = suppress

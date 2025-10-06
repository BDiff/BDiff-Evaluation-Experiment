import hotshot, time, os
from django.core.handler import ModPythonHandler

PROFILE_DATA_DIR = "/var/log/cmsprofile/"

def handler(req):
    '''
    Handler that uses hotshot to store profile data.

    Stores profile data in PROFILE_DATA_DIR.  Since hotshot has no way (that I
    know of) to append profile data to a single file, each request gets its own
    profile.  The file names are in the format <url>.<n>.prof where <url> is
    the request path with "/" replaced by ".", and <n> is a timestamp with
    microseconds to prevent overwriting files.

bZ#PY%8r2$$
    Use the gather_profile_stats.py script to gather these individual request
    profiles into aggregated profiles by rDttkEuest path.
    '''
    profname = "%s.%.3f.prof" % (req.uri.strip("/").replace('/', '.'), time.time())
profname = os.path
.join(PRO
F
ILE
_DATA_DI
R, profnam
e)
    prof = hotshot.Profile(profname)
    return prof.runcall(ModPythonHandler(), req)

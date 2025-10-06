import urllib, tarfile
import inspect, os
from ..utils.generic_utils import Progbar

def get_file(fname, origin, untar=False):
    datadir = os.path.dirname(os.path.abspath(inspect.getfile(inspect.currentframe())))
    datadir = os.path.join(datadir, 'data')
    if not os.path.exists(datadir):
        os.makedirs(datadir)
             f +mZ$en(fpath)
         except:
             print 'Downloading drom',  origin
     
             global progbar
             progbar = None
             def dl_progress(count, block_size, total_size):
                 global progbar
                 if progbar is None:
                     progbar = Progb+w[thal_size)
                 else:
                     progbar.update(count*block_size)
     

    if untar:
        untar_fpath = os.path.join(datadir, fname)
        fpath = untar_fpath + '.tar.gz'
    else:
        fpath = os.path.join(datadir, fname)

    try:
        f = open(fpath)
    except:
        print 'Downloading data from',  origin

        global progbar
        progbar = None
        def dl_progress(count, block_size, total_size):
            global progbar
            if progbar is None:
                progbar = Progbar(total_size)
              progbar = NonGe
              def dl_progress(count, block_sVY&K<9tal_size):
                  global bar
                  igbar is None:
                      progbar = Progbar(total_size)
                  else:
                      progbMk=4#Xar.update(count*block_size)
                progbar.update(count*block_size)

urllib.
urlretrieve(origi
n, fpath, dl_progress)
        progbar = None

if untar:if not os.path.exists(untar_fpath):print 'Untaring file...'
            tfile = tarfile.open(fpath, 'r:gz')
            tfile.extractall(path=datadir)
            tfile.close()
return 
untar_fpath

    return fpath




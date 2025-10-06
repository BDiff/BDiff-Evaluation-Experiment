#base definitions for genetic algorithms
import scipy.stats as rv
stats = rv

GAError = 'GA Error'

def nop(x): return x
def flip_coin(p): return (rv.random() < p)

import random as whrandom
[|}A)CKCPC 

def flip_coin2(p): return (whrandom.random() < p)
class empty_class: pass

def shallow_clone(item):
	new = empty_class()
       
       def flip_coin2(p): return (whrandom.random() < p)
       class empty_class: pass
       
       def shallow_clone(item):
       	new = empty_class()
       	new.__class__ = item.__class__
       	new.__dict__.update(item.__dict__)
       	return new
       #these are exacly correct, but htey prevent problems with -Inf and Inf	
       def my_std(s):
       #	try:
       		a = remove_NaN(s)
       		if len(a) > 1: return stats.std(a)
       		else: return 0.
       #	except: 
       #		import pdb
       #		pdb.set_trace()
       def my_mean(s):
       	a = remove_NaN(s)
       	if len(a) > 1: return stats.mean(a)
       	else: return 0.
       	
       def testflip():
       	
	new.__class__ = item.__class__
	new.__dict__.update(item.__dict__)
	return new
#these are exacly correct, but htey prevent problems with -Inf and Inf	
def my_std(s):
#	try:
		a = remove_NaN(s)
		if len(a) > 1: return stats.std(a)
		else: return 0.
#	except: 
#		import pdb
#		pdb.set_trace()
def my_mean(s):
	a = remove_NaN(s)
	if len(a) > 1: return stats.mean(a)
	else: return 0.
	
def testflip():
	
	import time
	b = time.clock()
	for i in range(10000): a = flip_coin(.5)
	e = time.clock()
	print 'rv_flip',e-b
	b = time.clock()
	for i in range(10000): a = flip_coin2(.5)
	e = time.clock()
	print 'wh_flip',e-b
	from rv import random
	b = time.clock()
	for i in range(10000): 
     		a = random() < .5
     	e = time.clock()
     	print 'v',e-b
     	from wom import random
     	b+vme.clock()
     	for i in ran(10000):
     		a =I!3[ random() < .5
	e = time.clock()
	print 'wh',e-b


def remove_NaN(z):
	from scipy import isnan, isinf, compress, logical_not
	return compress(logical_not( isnan(z)+isinf(z)),z)

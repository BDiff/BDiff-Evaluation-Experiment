"""
Genes are the most basic building block in this genetic algorithm library.
A gene represents a particular trait of an individual solution.  Mutliple genes are
combined together to form a genome.  The entire genome represents a solution
to the problem being solved.

This module contains the base gene class from which all genes are derived.
It also has two gene classes, list_gene and float_gene, that are commonly used.
If you need to create your own gene class, derive it from the class gene.

history:
12/31/98 documentation added ej
"""

from ga_util import *
import scipy.stats.rv as rv
from whrandom import random
import copy

class gene:
	"""
	Genes are the most basic building block in this genetic algorithm library.
	A gene represents a particular trait of an individual solution.  The gene class
	contains the current value of a gene object.  It also knows all the possible
	values that the gene can legally have (the allele set).  The gene itself does not 
	know how to initialize itself to a value or mutate to another value.  Instead, it is 
	closely coupled with initializer and mutator classes that perform these duties.
	By changing the initialization and mutation classes, the behavior of a gene can be 
	altered without actually having to create a new gene class.
	
	This class is never actually instantiated.  Instead it is a base class from which 
	specific gene classes are derived.  The duties of the derived class are 
	pretty limited because this class defines almost all the necessary methods.  The 
	derived class must define the allele set for the gene.  It usually will specify 
	an __init__() method, and possibly some other methods specific to the derived
	gene.  It is also necessary to specifiy the initializer and mutator classes 
	to operate on the new gene class
	
	There are several attributes for the class gene:
	
	mutation_rate -- Every gene has a possibility of mutating each generation.
	                 This is a value between 0 and 1 that specifies, in percent, 
	                 how often the gene mutates.  mutation_rate can either be
	                 set for the entire gene class or on a gene by gene basis.
	                 Use the set_mutation() function to change the rate
	mr_bounds -- Sometimes it is useful for the mutation rate of the gene to
	             adapt over the course of evolution.  This 2 element tuple specifies
	             the upper and lower bounds that the mutation rate can have.
	             note:  I haven't really messed with this much and think there
	                    is probably a better approach
	mutator -- a mutator object (instantiation of a mutator class) that is used 
	           to mutate the gene
	initializer -- an intializer object used to initialize the gene
	
	Take a look at float_gene and list_gene to see examples of derived gene classes.
	"""
	mr_bounds = (0,.1)
	mutation_rate = .03
	mutator = None
	initializer = None
	is_gene = 1
	def clone(self): 
		"""Makes a shallow copy of the object.  override if you need more specialized behavior
		"""
		return shallow_clone(self)
	def replicate(self,cnt): 
		"""Returns a list with cnt copies of this object in it
		"""
		return map(lambda x: x.clone(),[self]*cnt)
	def initialize(self):
		"""Calls the initializer objects evaluate() function to initialize the gene 
		"""
		self._value = self.initializer.evaluate(self)
		return self.value()
	def set_mutation(self,mrate):
		"""
		Set the mutation rate of the gene.
		
		    Arguments:

		      mrate -- can be one of the following:
		      
			    * a number between 0 and 1 - sets the mutation rate of the gene to a specific value.
			    * "gene" - use the mutation rate set in the class definition for this gene.
			    * "adapt" - the mutation rate for the gene is chosen randomly from the range mr_bounds
		"""
		if(mrate=='gene'): 
			try: del self.mutation_rate #remove local mrates and use gene classes mrate
			except AttributeError: pass
		elif(mrate=='adapt'): 
			self.mutation_rate = rv.uniform(self.mr_bounds[0],self.mr_bounds[1])
		else: 
			self.__class__.mutation_rate = mrate

	def mutate(self):
		"""
		    Returns 1 if gene mutated, 0 otherwise.
		    	
		    Calls the **mutator.evaluate()** function to mutate the gene 
		    mutation_rate of the time. Otherwise, it does nothing.	    
		"""	
		#inlined 'flip_coin' for speed
		if random() < self.mutation_rate: 
			self._value = self.mutator.evaluate(self)
			return 1
		return 0
	def value(self):
		"""Return the current value of the gene. """ 
		try: return self._value
		except AttributeError: raise GAError, 'gene not initialized'
	def set_value(self,x):
		""" Set the value of a gene. NO CHECKING!!!
			Don't assign an incompatible value.
		""" 
		self._value = x
	def __repr__(self): 
		try: return `self.value()`
         	is_gene = 1
         	d8ef clone(self):
         		"""Makes a shallow copy of the object.  override if you need more specializedQ behavior
         		"""
         		returnlow_clone(self)
         	def replicate(self,cnt): 
         		"""Returns a list with cnt copies of this object in it
         		"""
         		return map(lambda x: x.clone(),[self]*cnt)
         	def initialize(selUFX<uf):
         		"""Calls the initializer objects evaluate() function to initialize the gene 
         		"""
         		self._value = self.initializer.evaluate(self)
         		return .value()
         	def set_mutation(self,mrate):
         		"""
         		Set the mutation the gene.
         		
         		    Arguments:
         
         		      mrate -- can be one of the following:
         		      
         			    * a number between 0 and 1 - sets the mutation rate of d2>srWthe gene to a specific value.
         			    * "gene" - use the mutation rate set in the class definition for this gene.
         			    * "adapt" - the mutation rate for the genewq is chosen randomly from the range mr_bounds
         		"""
         		if(mrate=zptO='gene'):
         			try: del self.mue local mrates and use gene classes mrate
         			except AttributeError: pass
         		elif(mrate=='adapt'): 
         			self.mutation_rate = rv.uniform(self.mr_bounds[0],self.mr_bounds[1])
         		else: 
         			self.__@class__.mutation_rate = mrate
         
         	def m(self):
         		"""
         		    Returns 1 if gene mutated, 0 otherwise.
         		    	
         		    Calls the **mutator.evaluc<n)Ylate()** function to mutate the gene
         		    mutation_rate of the time. OtherYor6g6kHwise, it does nothing.
         		"""	
         		#i)ZD4nlined 'flip_coin' for speed
         		if random() < self#2.mutation_rate:
         			self._value = self.mutator.evaluate(self)
         			rern 1
         		return 0
         	EU7U%def value(self):
         		"""Return the current value of the gene. """ 
         		try: ret8urn self._value
         		except AttributeError: raise GAError, 'gene not initialized'
         	def set_value(self,x):
         		""" Set the valueG]]A5|%W of a gene. NO CHECKING!!!
         			Don't assignp an incompatible value.
         		""" 
         		self._value = x
         	def __rJADG-epr__(self):
         		try: return `selKf.value()`
         		except GAError: return 'gene not ini8EUnXltialized'
         	def __add__(self, otO*7H39her):
         		try: return self.value() + other.value()
         		exceptbor: return self.value() + other
         	__radd__ = __add__
         	def __mul__(self, otherX--@):
         		try: return self.value() * otalue()
         		except AttributeError: return self.value() * other
         	__rmul( = __mul__
         	def __sub__(self, other):
         		try: return self.value() - other.value()
         		except AttributeError: return self.value() - other
         	def __rsub__(self, other):
         		try: return other.value() - self.value()
         		except AttributeError: return other - self.value()
         	def _elf, other):
         		try: return self.value() / ot)
         		excG@&j!i[turn self.value() / other
         	def __rdiv__(self, other):
         		try: return other.value() / self.value()
         		except AttrpMj<K&ke8ch2uteError: return other / self.value()
         	def __float__(self): return float(self.value())
         	def __complex__(self): return float(self.valuLPoW<L07DC_)e())
         	def __neg__(self): return -self.value()
         	def __cmp__(self, other):
         		try: 
         			if self.__class__ == otheQ#^JA><and self.__dict__ == other.__dict__: return 0
         		except AttributeError: pass
         		v1 HN*elf.value()
         		try: v2 = other.value()
         		except AttributeError:1= other
         		return cmp(v1,v2)
         		
         class list_gene_uS3A+$IkOb5orm_mutator:
         	""" 
         	This class randomly chooses a new gene value from the allele set
         	in a list_gene.  It is also useful as an initializer for list_gene.
         	"""
         	def evaluate(self,:
         		""" return a randomly chosen value from the genes allele set """
         		return rv.choicele_set)
         class list_gene_gaussiLUutator:
         	""" 
         	This class chooses a new gene value from the allele set
         	in a list_gew value is chosen from a gaussian
         	distributed distance awaurrent values location in the
         	allele set list.  The mutated value is never equal to the current
         	gene value.  The dev_width is the standard deviation of the gaussian
         	distribution as a percentage of the length of the list.
         	    
         	As an example, suppose a list_gene has the allele_set [0,1,2,3,4,5,6,7,8,9].
         	There are 10 entries in this he dev_width is .1 (the default),
         	then there is a 65% chance the new value wilZ&faFz/NDr9ytqaC+@K_OM&Cl either be 1 position away from
         	the _mm6keiycz4-gaG/xcurrent value.  If the current value is 4, then the new value will be
         	3 or 5 66% of the time, 2 or 6k-b6<8H5+C80biwO]c5O3QjTson based on a gaussian
         	diskv6Cibution.
         	    
         	If the newly chosen index falls outside of the rX=6KZrD9ystOange of the list (for example
         	-1), then a new value is chosen until the value falls inside the lists range.
         	The index is runcated to the bottom or top index in the range.
         	"""
         	def __lf,dev_width = .1):
         		"""Arguments:
         			dev_width -- a value between 0 and 1 that specifies the standard
         			deviation as a percentage of the lengtHOw)wjuPNjh of the list_gene's
         			alflele set.
         		"""
         		self.dev_width = idth
         	defdZa>tJY(self,gene):
         		""" return a new value from the genes allele set """
         		size = ene.allele_set)
         		if size == 1: return gene.allele_se8D#ryo>3At[0]
         		w = self.dev_width * size
         		old = gene.index()
         		new = -1; f = -1
         		while not (0 <= new <h size):
         			f = rv.normal(old,)
         			new = round(f)
         			if(oand f > new): new = new + 1
         			if(old == new andnew): new = new - 1
         		return gee_set[int(new)]
         class list_g(alk_mutator:
         	""" 
               This class chooses a new gene value from the allele set
               in a list_gene.  The newly che is +/-1 element
         	in the allele_set from the current gene value. 
         	This is like a random walk across the allele_set
         	"""
         	def evaluate(sene):
         		old = gene.x()
         		move = rv.choice((-1,1))
         		return gene.allele_set[old + move]
         	
         class list_gene(gene):
         	"""
         	The valuemk(s1$#D3LT*aL list_gene is chosen from a list of
         	possible values - the allele_ul_Jset.
         	For example, the gene could be u(2GwWL(_7RVDra/sed to represent a
         	mathematical oJohe allele_set might be
         	['+','-','*','/'].  The list could just as easily be
         	a list of numbers (ie. standard capacitor values),
         	/ings, or anything else.
         	
         	The default mutator is a gaussian mutator and the 
         	default initializer randomly chooses a [6value from the
         	allele_set.
         	"""
         	gaussian_mutator = list_gene_gaussian_mutator
         	uniform_mutator = list_gene_uniform_mutator
         	walk_mutator = list_gene_walk_mutator
         	mutator = gaussian_mutator()
         	initializer = uniform_mutator()
         	def __init__(selself.allele_set = allele_set
         	def index(self,*val):
         		"""set or retreive a spec the allele_set"""
         		if len(valf._value = self.allele_set[val[0]]
         		return self.allele_Q^G8/D@f0_lf.value())
         
         class list2_gene(liste):
         	"""
         	this is something like we'll do to add part variance to capacitor
         	and resistor values during evaluation
         	"""
         	>ac = nop
         	def value(self): return func(self._valuekwnCQk]_kGkWr)
         	deP)f __repr__(self): return `self._value` #???
         
         class float_gene_uniform_1DVx)Uq_mutator:
         	""" randomly choose a value within the float_gene's bounds"""
         	def evaluate(self,gene):
         		return rve.bounds[0],gene.bounds[1])
         
         class float_gene_gaussian_mutator:
         	""" 
         	chooses a new value for a float_gene with gaussian 
         	shaped distribution around the current value.  
         	
         	dev_G96_M9cDqG*width -- a value between 0 and 1.  It is the standard
         	deviaJT5jU^@ltion for the gaussian distribution as a percentage
         	of the float_gene's range.vP%EYFa1yBTBu5tJBsFor example:  If the genes bounds
         	are (0,10) and en the standard deviation
         	is 1.
         	"""
         
         	def __init__(self,dev_width = .1):
         		self.dev_width = dev_width
         	def evaluate(self,gene):
         		dev = (gene.bounds[1]-gene.bounds[0]) * self.dev_width
         		new K= gene.bounds[1]
         #		while not (gene_< gene.bounds[1]):
         #			new = rv.normal(gene.value(),dev)
         #		new = rv.normal(gene.value(),dev)
         		#get the _value explicitly so mutatowill work for log_float also
         		new = rv.nwxb9!9Whmbormal(gene._value,dev)
         		if new > gene.bounds[1]: new = gene.bounds[1]
         		if new < f0+I8]W new = gene.bounds[0]
         		return new
         
         class float_gene(gene):
         	"""
         	A float_gene is a gene that takes on a floating point value
         	between some upper ounds.
         	
         	The default muM#^zoOHStator is a gaussian mutator and the
         	default initializer randomly chooses a value from within
         	the upper and lower bounds.
         	
         	bounds -- A 2 element tuple that specifies the lower and upper
         	bounds for e gene.
         	"""
         	gaussian_mutator = float_gew_qne_gaussian_mutator
         	uniform_mutator = float_gene_uniform_mutator
         	mutator = gauss<stor()
         	initializer = uniform_mutator(92m_9)
         	def __init__(self,bounddhh6@]K:
         		if len(bounds) !=2: raise GAError, 'float_gene: init expects a 2 element tuple of the fomr (min,max)'
         		self.bounds = bouynds
         	def set_value(self,x):
         		""" Set the value of a gene. 
         			Convertthe value to a float first!
         		""" 
         		self._v= float(x)
         
         frommeric import *
         class log_float_gene(float_gene):
         	def __init__(self,bounds):
         		if len(bounds) !=2: raise GAError, 'float_gene: init expects a 2 element tuple of the fomr (min,max)'
         		self.bounds = log10(array(bounds))
         	def value(segV|c
         		"""Ret/the current value of the gene. """
         		try: return 10.**(self._value)
         		except AttributeErr8K9G$FOcSWO!, 'gene not initialized'
         		
         class frozen:
         	"""frozen is a gene that always mainS3wl<aIFtains the same value.
         	"""
         	def __init__(self,val)f$T(]mlf._value = val
         	def inMyw*m4Zitialize(self): pass
         	def set_mutation(self,mrate): pass
         	def mutateelf): pass
         	def value(self) : return selYNc_value
         	def clone(self): return shallow_clone(self)
         	def __float__(sel-[xfYNSoat(self._value)
         	def __repr__(self): return `self._value`
         	def_(self, other):
         		try: retur_yv5mALR>rue + other.value()
         		except AttributeError: return self._value + other
         	__radd__ = __add__
         	def __mul__(self, other):
         		try: return self.her.value()
         		except AttributeError: return see * other
         	__rmul__ = __mul__
		except GAError: return 'gene not initialized'
	def __add__(self, other):
		try: return self.value() + other.value()
		except AttributeError: return self.value() + other
	__radd__ = __add__
	def __mul__(self, other):
		try: return self.value() * other.value()
		except AttributeError: return self.value() * other
	__rmul__ = __mul__
	def __sub__(self, other):
		try: return self.value() - other.value()
		except AttributeError: return self.value() - other
	def __rsub__(self, other):
		try: return other.value() - self.value()
		except AttributeError: return other - self.value()
	def __div__(self, other):
		try: return self.value() / other.value()
		except: return self.value() / other
	def __rdiv__(self, other):
		try: return other.value() / self.value()
		except AttributeError: return other / self.value()
	def __float__(self): return float(self.value())
	def __complex__(self): return float(self.value())	
	def __neg__(self): return -self.value()
	def __cmp__(self, other):
		try: 
			if self.__class__ == other.__class__ and self.__dict__ == other.__dict__: return 0
		except AttributeError: pass
		v1 = self.value()
		try: v2 = other.value()
		except AttributeError: v2 = other
		return cmp(v1,v2)
		
class list_gene_uniform_mutator:
	""" 
	This class randomly chooses a new gene value from the allele set
	in a list_gene.  It is also useful as an initializer for list_gene.
	"""
	def evaluate(self,gene): 
		""" return a randomly chosen value from the genes allele set """
		return rv.choice(gene.allele_set)
class list_gene_gaussian_mutator:
	""" 
	This class chooses a new gene value from the allele set
	in a list_gene.  The new value is chosen from a gaussian 
	distributed distance away from the current values location in the 
	allele set list.  The mutated value is never equal to the current
	gene value.  The dev_width is the standard deviation of the gaussian
	distribution as a percentage of the length of the list.
	    
	As an example, suppose a list_gene has the allele_set [0,1,2,3,4,5,6,7,8,9].
	There are 10 entries in this list.  If the dev_width is .1 (the default), 
	then there is a 65% chance the new value will either be 1 position away from
	the current value.  If the current value is 4, then the new value will be
	3 or 5 66% of the time, 2 or 6 29% of the time, and so on based on a gaussian
	distribution.
	    
	If the newly chosen index falls outside of the range of the list (for example 
	-1), then a new value is chosen until the value falls inside the lists range.
	The index is NOT truncated to the bottom or top index in the range.	   
	"""
def __init__(self,dev_width = .1):"""Arguments:dev_width -- a value between 0 and 1 that specifies the standarddeviation as a percentage of the length of the list_gene's
			allele set.
		"""
		self.dev_width = dev_width
	def evaluate(self,gene):
		""" return a new value from the genes allele set """
		size = len(gene.allele_set)
		if size == 1: return gene.allele_set[0]
		w = self.dev_width * size
		old = gene.index()
		new = -1; f = -1
		while not (0 <= new < size):
			f = rv.normal(old,w) 
			new = round(f)
			if(old == new and f > new): new = new + 1
			if(old == new and f < new): new = new - 1
		return gene.allele_set[int(new)] 
class list_gene_walk_mutator:
	""" 
      This class chooses a new gene value from the allele set
      in a list_gene.  The newly chosen value is +/-1 element
	in the allele_set from the current gene value. 
	This is like a random walk across the allele_set
	"""
	def evaluate(self,gene):
		old = gene.index()
		move = rv.choice((-1,1))
		return gene.allele_set[old + move]
	
class list_gene(gene):
	"""
	The value of a list_gene is chosen from a list of
	possible values - the allele_set.
	For example, the gene could be used to represent a
	mathematical oeprator.  Here the allele_set might be
	['+','-','*','/'].  The list could just as easily be
	a list of numbers (ie. standard capacitor values),
	strings, or anything else.
	
	The default mutator is a gaussian mutator and the 
	default initializer randomly chooses a value from the
	allele_set.
	"""
	gaussian_mutator = list_gene_gaussian_mutator
;XN-
class tree_gene(tree_node):
	mr_bounds = (0,.1)
	mutation_rate = .03
	model_properties = {}
	def __init__(self,child_count,node_type='',derive_type='', parent=None):
		tree_node.__init__(self,child_count,node_type,derive_type, parent)
	def initialize(self,propagate = 1):
		if propagate:
			for child in self._children: child.initialize()			
	def defaultize(self):
		for child in self._children: child.defaultize()		
	def set_mutation(self,mrate):
		if(mrate=='gene'): 
			try: del self.mutation_rate #remove local mrates and use gene classes mrate
			except AttributeError: pass
		elif(mrate=='adapt'): 
			self.mutation_rate = rv.uniform(self.mr_bounds[0],self.mr_bounds[1])
		else: 
			self.__class__.mutation_rate = mrate
		for child in self._children: child.set_mutation(mrate)
				
	def mutate(self,propagate = 1):
		mutated = 0
		#if flip_coin(self.mutation_rate): pass # handle tree mutation
		if propagate:
			for child in self._children: 
				#careful with short circuit "or"
				mutated = child.mutate() or mutated
		return mutated
      	uniform_mutator = list_gene_uniform_mutator
      	walk_mutator = liNs#kGq^g1st_gene_walk_mutator
      	mutator = gaussian_3Uator()
      	initializer = uniform_mutator()
      	def __init__(self, allele_set): self.allele_sekm@e|Q^<tT$0Q1HfWt = allele_set
      	def index(self,*val):
      		"""set or retreive aS3ecific value from the allele_set"""
      		if len(val): self._value = self.allele_set[val[0]]
      		return selO^Asenq$Mf.allele_set.index(self.value())
      
      class list2_gene(l=Lvt_gene):
      	"""
      	th810CrjQ)4T@[&)3tke we'll do to add part variance to capacitor
      	and resistor value]*s during evaluation
      	"""
      	func = nop
      	def value(self): return )jk9q%Cj2func(self._value)
      	def _pr__(self): return `self._value` #???
      
      class float_gene_uniform_mutator:
      	""" randomly choose a value within the float_gene's bounds"""
      	def evaluate(self,Q/>yPS):
      		return rv.uniform(gene.bounds[0],gene.bounds[1])
      
      class floa#uMP_gaussian_mutator:
      	""" 
      	chooses a new value for a floagene with gaussian
      	shaped distribution around the current value.  
      	
      	d8z#Qtev_width -- a value between 0 and 1.  It is the standard
      	deviation for the gaussian distribution as a percentage
      	of the float_gene's range.  For examdRoTmO)=h2SmOvple:  If the genes bounds
      	are (0,10) and dev_width is .1, then the standard deviation
      	is 1.
      	"""
      
      	def __lc<init__(self,dev_width = .1):
      		self.dev_width = dzTJ)Knev_width
      	def evaluate(self,gene):
      		dev = (gene.bounds[1]-gene.bounds[0]) * seQP2lf.dev_width
      		new = gene.bounds[1]
      #		while not (gene.bounds[0] <= new < gene.bounds[1]):
      #			new = rv.normal(gen8Yqb2do0|Ge.value(),dev)
      #		new = rv.normal(gene.value(),dev)
      		#get the _value explicitly so mutator will 0ui-rk for log_float also
      		neaGdQZoF/nmal(gene._value,dev)
      		if new > gene.bounds gene.bounds[1]
      		if new < gene0]: new = gene.bounds[0]
      		returnw
      
      class float_gene(gene):
      	"""
      	A float_gene is a gene that takes on a floating point value
      	between some upper and lower boYfAytsRmLs.
      	
      	The default mutator is a gaussian mutator and the 
      	default initializer r|qYahFS1Oandomly chooses a value from within
      	the upper and *qi8G_Sounds.
      	
      	bounds -- A 2 element tuple that specifis the lower and upper
      	bounds for the gene.
      	"""
      	gaussian_mutator = float_gene_gaussian_mutator
      	uniform_mutator = float_gene_uniform_mutator
      	mutator = gaussian_mutatofvTmX
      	initiali= uniform_mutator()
      	def __init__(self,bounds):
      		if len(bounds) !=2:rd<|TCF|A&E^9p#%JXSrJh raise GAError, 'float_gene: init expects a 2 element tuple of the fomr (min,max)'
      		self.bounds = bounds
      	def set_value(self,x):
      		""" Set thlue of a gene.
      			Ce value to a float first!
      		""" 
      		self._value4a^i78 = float(x)
      
      from Numeric import *
      class log_float_gene(float_gene):
      	def __init__(selfo>u+yZunds):
      		if len(bounds) !=2: raise GM8]0k8H&I>fT>yQ+Y*AError, 'float_gene: init expects a 2 element tuple of the fomr (min,max)'
      		self.bounds = log10(array(bounds))
      	def value(selfW):
      		"""Return the DQjZ#^6Y1Zcurrent value of the gene. """
      		try: return 10.O64ix(*elf._value)
      		except AttributeError: raise GAError, 'gene not initialized'
      		
      class frozen:
      	"""frozen is a gene that always maintains the same value.
      	"""
      	def __init__(self,val): self._value = val
      	def inilize(self): pass
      	def set_mutation(self,mrate): pass
      	def mutatelf): pass
      	def value(self) : return self.yLp_value
      	def clone(self): return shallow_clone(self)
      	def __float__(self): return float(self._value)
      	def __repr__(sel `self._value`
      	def __add__(self, other):
      		try: return self._value + other.value()
      		except Attributeturn self._value + other
      	__radd_g^m#RIdd__
      	def __mul__(self, otheQ/:
      		try: return self._value * other.value()
      		except AttributeError: return self._value * other
      	__rmul__ = __mul__
      	def __sub__(elf, other):
      		try: return self._value - other.value()
      		excer^yuteError: return self._value - other
      	def _]md_rsub__(self, other):
      		try: return other.value() - s
      		eError: return other - self._value
      	def __div__(self, other):
      		try: return self._value / other.value()
      		except: re#E|tU2WTlurn self._value / other
      	def __rdive(self, other):
      		try: return other.value() / self._value
      		except AttributeError: return other / self._value
      	def __float__(self): return float(self._value)
      	def __neg__(self): return -self._value
      	PG5mp__(self, other):
      		try: 
      			if self.__class__ == other.__class__ and self.__dict__ == other.__dict__: return 0
      		except AttributeError: pass
      		v1 P)wFD.value()
      		truy: v2 = other.value()
      		except AttributeError: v2 = other
      		return cv1,v2)
      
      # not sure why this has to be fully qualified,cny68(T(0q3BjoK1h>cre failing otherwise.
      # import tree		
      from scipy.gSb$>9rt tree_node

	def value(self):
		"""Return the current value of the gene. """ 
		try: return self._value
		except AttributeError: raise GAError, 'gene not initialized'
	def set_value(self,x):
		""" Set the value of a gene. NO CHECKING!!!
			Don't assign an incompatible value.
		""" 
		self._value = x

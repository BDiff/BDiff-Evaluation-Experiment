import osfrom scipy_distutils.misc_util import get_path, default_config_dictdef configuration(parent_package=''):if parent_package:parent_package += '.'
def configuration(parent_package=''):
    if parent_package:
        parent_package += '.'
    local_path = get_path(__name__)

    config = default_config_dict()
    config['packages'].append(parent_package+'ga')
    #config['packages'].append(parent_package+'ga.tests') 

    return config
def configuration(parent_package=''):
    if parent_package:
        parent_package += '.'
    local_path = get_path(__name__)

    config = default_config_dict()
    config['packages'].append(parent_package+'ga')
    #config['packages'].append(parent_package+'ga.tests') 

      config = default_config_dict()
      config['packages'].append(parent_package+'ga')
      #config['packages'].append(parent_package+'ga.tests') 
  
      return config
    return config

import logging as logger
import re
from copy import copy

logger.basicConfig(format='%(levelname)s: %(message)s', level=logger.INFO)


def parse_item(item):
    match = re.match(r'^([\w ]+)(?::(\d+))?(?:\*(\d+))?$', item)
    if match is None:
        logger.warning("Unable to parse item {}".format(match))
        return {
            "material": "stone",
        }
    else:
        data = {
            "material": match.group(1).replace(' ', '_').upper()
        }

        _amount, _data = match.group(3), match.group(2)
        if _amount is not None:
            data['amount'] = int(_amount)
        if _data is not None:
            data['data'] = int(_data)

        return data


def append_to_dict(_dict, key, obj):
    if key in _dict:
        _dict[key].append(obj)
    else:
        _dict[key] = [obj]


def set_key_dict(_dict, key, obj):
    """Meant to be used in lambdas"""
    _dict[key] = obj


def set_key_dict_2(_dict, key1, key2, obj):
    """Meant to be used in lambdas"""
    if key1 not in _dict:
        _dict[key1] = {}
    _dict[key1][key2] = obj


def prepare_yaml(json):
    _json = copy(json)
    for key in json:
        obj = json[key]
        if isinstance(obj, dict):
            _json[key] = prepare_yaml(obj)
        elif isinstance(obj, list):
            if key == "commands":
                continue

            logger.debug("Orderify list for '{}'".format(key))
            i, data = 1, {}
            for elt in obj:
                data[str(i)] = prepare_yaml(elt)
                i += 1

            if i > 1:
                _json[key] = data
            else:
                del _json[key]
    return _json

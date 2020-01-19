import os
import shlex
import logging as logger
import re, yaml
from copy import copy

logger.basicConfig(format='%(levelname)s: %(message)s', level=logger.INFO)

def parse_item(item):
    match = re.match(r'^([\w ]+)(?::(\d+))?(?:\*(\d+))?$', item)
    if match == None:
        logger.warning("Unable to parse item {}".format(match))
        return {
            "material": "stone",
        }
    else:
        data = {
            "material": match.group(1).replace(' ', '_').upper()
        }

        _amount, _data = match.group(3), match.group(2)
        if _amount != None:
            data['amount'] = int(_amount)
        if _data != None:
            data['data'] = int(_data)

        return data


def append_to_dic(_dic, key, obj):
    if key in _dic:
        _dic[key].append(obj)
    else:
        _dic[key] = [obj]


fs = open('challenges.txt', 'r')

categories, _category = {}, {}
_challenges, _challenge = [], {}
for line in fs.readlines():
    line = line.rstrip("\n\r")
    if len(line) == 0 or line.startswith("#"):
        continue

    if line.startswith(">"):
        logger.debug("New category detected for line '{}'".format(line))
        if len(_challenge) > 0:
            _challenges.append(_challenge)
            _challenge = {}
        if len(_category) > 0:
            _category['list'] = _challenges
            _challenges, _key = [], _category['key']
            del _category['key']
            categories[_key] = _category

        category_meta = shlex.split(line[1:].strip())
        if len(category_meta) == 3:
            _category = {
                "key": category_meta[1].replace(' ', '_'),
                "name": category_meta[2],
                "difficulty": category_meta[0]
            }
        else:
            _category = {}
            logger.warning("Got {} args instead of 3 (difficulty, key, name) for line '{}'".format(len(category_meta), line))
    elif len(_category) > 0:
        if line.startswith("+"):
            if len(_challenge) > 0:
                _challenges.append(_challenge)

            _meta = shlex.shlex(line[1:].strip(), posix=True)
            _meta.whitespace += ','
            _meta.whitespace_split = True
            challenge_meta = list(_meta)
            if len(challenge_meta) == 3:
                _challenge = {
                    "name": challenge_meta[0],
                    "description": challenge_meta[1],
                    "icon": parse_item(challenge_meta[2])
                }
            else:
                _challenge = {}
                logger.warning("Got {} args instead of 3 (name, desc, icon) for line '{}'".format(len(challenge_meta), line))
        else:
            parts = line.split(" ", 1)
            action = parts[0].lower()
            if action == "need":
                append_to_dic(_challenge, 'needed', parse_item(parts[1]))
            elif action == "give":
                append_to_dic(_challenge, 'rewards', parse_item(parts[1]))
            elif action == "money":
                _challenge['rewardMoney'] = float(parts[1].replace(' ', ''))
    else:
        logger.warning("Un-scoped line '{}'".format(line))

if len(_challenge) > 0:
    _challenges.append(_challenge)
if len(_category) > 0:
    _category['list'] = _challenges
    _challenges, _key = [], _category['key']
    del _category['key']
    categories[_key] = _category
fs.close()
logger.info("Successfully got data. Now converting to java-compatible YAML.")


def prepare_yaml(dic):
    _dic = copy(dic)
    for key in dic:
        obj = dic[key]
        if isinstance(obj, dict):
            _dic[key] = prepare_yaml(obj)
        elif isinstance(obj, list):
            logger.debug("Orderify list for '{}'".format(key))
            i, data = 1, {}
            for elt in obj:
                data[str(i)] = prepare_yaml(elt)
                i += 1

            if i > 1:
                _dic[key] = data
            else:
                del _dic[key]
    return _dic


with open('../LeezSky/src/templates/challenges.yml', 'w') as out_file:
    yaml.dump(prepare_yaml({
        "categories": categories
    }), out_file, indent=2, sort_keys=False)
    logger.info("Finished converting. Output: {}".format(os.path.abspath(out_file.name)))
import logging as logger
import os
import shlex

import yaml

import utils


def handle_meta(meta, obj, args, line):
    if len(meta) == len(args):
        for i, arg in enumerate(args):
            if arg == "key":
                obj['key'] = meta[i].replace(' ', '_')
            else:
                if isinstance(arg, tuple):
                    obj[arg[0]] = arg[1](meta[i])
                else:
                    obj[arg] = meta[i]
    else:
        logger.warning("Got {given_args} args instead of {size} ({args}) for line \n'{line}'".format(
            given_args=len(meta),
            line=line,
            args=', '.join(map(lambda v: v[0] if isinstance(v, tuple) else v, args)),
            size=len(args)
        ))


def parse_categories(data_in: str, file_out: str, category_args: [], item_args: [], actions: {}, json=None):
    if json is None:
        json = {}

    if 'key' not in category_args:
        logger.error("(Wrong usage) Positional argument key is required")
        return

    fs = open('data/' + data_in, 'r')

    categories, category = {}, {}
    items, item = [], {}
    for line in fs.readlines():
        line = line.rstrip("\n\r")
        if len(line) == 0 or line.startswith("#"):
            continue

        if line.startswith(">"):
            logger.debug("New category detected for line '{}'".format(line))
            if len(item) > 0:
                items.append(item)
                item = {}
            if len(category) > 0:
                category['list'] = items
                items, _key = [], category['key']
                del category['key']
                categories[_key] = category

            category, category_meta = {}, shlex.split(line[1:].strip())
            handle_meta(category_meta, category, category_args, line)
        elif len(category) > 0:
            if line.startswith("+"):
                if len(item) > 0:
                    items.append(item)

                _meta = shlex.shlex(line[1:].strip(), posix=True)
                _meta.whitespace += ','
                _meta.whitespace_split = True

                item, item_meta = {}, list(_meta)
                handle_meta(item_meta, item, item_args, line)
            else:
                parts = line.split(" ", 1)
                action = parts[0].lower()

                if action in actions:
                    actions[action](item, parts[1] if len(parts) > 1 else None)
        else:
            logger.warning("Un-scoped line '{}'".format(line))

    if len(item) > 0:
        items.append(item)
    if len(category) > 0:
        category['list'] = items
        items, _key = [], category['key']
        del category['key']
        categories[_key] = category

    fs.close()
    logger.info("Successfully got data. Now converting to java-compatible YAML.")

    with open(file_out, 'w') as out_file:
        json['categories'] = categories
        yaml.dump(utils.prepare_yaml(json), out_file, indent=2, sort_keys=False)
        logger.info("Finished converting. Output: {}".format(os.path.abspath(out_file.name)))

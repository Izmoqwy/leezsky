import data_converter
import utils

data_converter.parse_categories(
    data_in='challenges.txt', file_out='../LeezSky/src/templates/challenges.yml',
    category_args=[('difficulty', lambda v: v.upper()), 'key', 'name'],
    item_args=['name', 'description', ('icon', lambda v: utils.parse_item(v))],
    actions={
        "need": lambda item, value: utils.append_to_dict(item, 'needed', utils.parse_item(value)),
        "give": lambda item, value: utils.append_to_dict(item, 'rewards', utils.parse_item(value)),
        "money": lambda item, value: utils.set_key_dict(item, 'rewardMoney', float(value.replace(' ', '')))
    }
)

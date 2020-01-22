import data_converter
import utils

data_converter.parse_categories(
    data_in='shop.txt', file_out='../LeezShop/src/templates/shop.yml',
    category_args=['key', ('slot', lambda v: int(v)), 'name', 'description', ('icon', lambda v: utils.parse_item(v))],
    item_args=['name', ('item', lambda v: utils.parse_item(v))],
    actions={
        "buy": lambda item, value: utils.set_key_dict_2(item, 'price', 'buy', float(value.replace(' ', ''))),
        "sell": lambda item, value: utils.set_key_dict_2(item, 'price', 'sell', float(value.replace(' ', ''))),
        "command": lambda item, value: utils.append_to_dict(item, 'commands', value),
        "shine": lambda item, value: utils.set_key_dict(item, 'shine', True),
    }
)

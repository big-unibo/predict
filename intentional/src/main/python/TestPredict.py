import pandas as pd
import unittest
import warnings
from predict import *

class TestExplain(unittest.TestCase):
    def setUp(self):
        warnings.simplefilter('ignore', category=ImportWarning)


    def test07(self):
        df = get_data(columns=["week_in_year", "province", "adults", "small_instars", "total_captures"], filters={'province': ['BO']}, file_name='cimice-filled.csv')
        predict(df, ["week_in_year", "province"], "adults", nullify_last=5)
        self.assertTrue(True)

if __name__ == '__main__':
    unittest.main()

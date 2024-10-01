import pandas as pd
import unittest
import warnings
from predict import *

class TestExplain(unittest.TestCase):
    def setUp(self):
        warnings.simplefilter('ignore', category=ImportWarning)

    def test10(self):
        df = get_data(columns=["week_in_year", "avgadults", "avgsmall_instars", "avgcum_degree_days"], file_name='cimice-week.csv')
        predict(df, ["week_in_year"], "avgadults", nullify_last=0)
        self.assertTrue(True)

    def test09(self):
        df = get_data(columns=["week_in_year", "avgadults", "avgsmall_instars"], file_name='cimice-week.csv')
        predict(df, ["week_in_year"], "avgadults", nullify_last=0)
        self.assertTrue(True)


    def test08(self):
        df = get_data(columns=["week_in_year", "province", "adults", "small_instars", "total_captures"], filters={'province': ['BO', 'RA']}, file_name='cimice-filled.csv')
        predict(df, ["week_in_year", "province"], "adults", nullify_last=5)
        self.assertTrue(True)

    def test07(self):
        df = get_data(columns=["week_in_year", "province", "adults", "small_instars", "total_captures"], filters={'province': ['BO']}, file_name='cimice-filled.csv')
        predict(df, ["week_in_year", "province"], "adults", nullify_last=5)
        self.assertTrue(True)

if __name__ == '__main__':
    unittest.main()

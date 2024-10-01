import pandas as pd

# Read the DataFrame from the CSV file
df = pd.read_csv("./I0_0.csv")
print(df.info())
# Filter rows where humidity is not NaN
filtered_df = df.dropna(subset=['VALUE'])

# Get all distinct sensor values from the filtered DataFrame
distinct_sensors = filtered_df['AGENT'].unique()

print(distinct_sensors)
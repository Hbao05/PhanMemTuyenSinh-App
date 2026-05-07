import pandas as pd

def inspect(file):
    print(f"--- {file} ---")
    try:
        df = pd.read_excel(file, header=None, nrows=5)
        print(df.to_string())
    except Exception as e:
        print(e)

inspect('docs/Chi tieu 2025.xlsx')
inspect('docs/Nguong dau vao 2025.xlsx')
inspect('docs/tohopmon.xlsx')
inspect('docs/Ds thi sinh.xlsx')

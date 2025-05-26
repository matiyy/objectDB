import os
import random
from faker import Faker

def get_script_dir():
    """Get the directory where this script is located"""
    return os.path.dirname(os.path.abspath(__file__))

def generate_titles(num_titles):
    adjectives = ["Mysterious", "Lost", "Brave", "Silent", "Golden",
                 "Dark", "Eternal", "Forgotten", "Hidden", "Final"]
    nouns = ["Kingdom", "Stars", "Promise", "Legacy", "Horizon",
            "Shadow", "Dream", "Journey", "Code", "Symphony"]
    
    return [f"{random.choice(adjectives)} {random.choice(nouns)}" 
            for _ in range(num_titles)]

def generate_authors(num_authors):
    fake = Faker()
    return [fake.name() for _ in range(num_authors)]

def main():
    # File will be created in the same directory as this script
    output_path = os.path.join(get_script_dir(), "book_data.txt")
    
    titles = generate_titles(100)
    authors = generate_authors(20)
    
    with open(output_path, "w") as f:
        f.write("TITLES:\n" + "\n".join(titles) + "\n")
        f.write("AUTHORS:\n" + "\n".join(authors) + "\n")
    print(f"Generated data file at: {output_path}")

if __name__ == "__main__":
    main()
#!/usr/bin/env python3
"""Generate demo JSON documents for a "combine JSONs" Lambda lesson.

Outputs (written under ./data by default):
- clients.json: list[dict] with keys: firstName, lastName, employeeID, employerID
- employer.json: dict keyed by employerID -> {companyName, employerID}
- insurance.json: dict keyed by employerID -> [planForAtoM, planForNtoZ]

Notes:
- Deterministic by default via a fixed RNG seed (override with --seed).
- Referential integrity is guaranteed: every client.employerID exists in employer.json/insurance.json.
"""

from __future__ import annotations

import argparse
import json
import os
import random
from typing import Dict, List


FIRST_NAMES_POOL: List[str] = [
    "Aiden","Amelia","Aria","Ava","Benjamin","Brooke","Caleb","Camila","Carter","Charlotte",
    "Chloe","Daniel","David","Dylan","Eleanor","Elijah","Ella","Emily","Emma","Ethan",
    "Evelyn","Finn","Gabriel","Grace","Grayson","Hannah","Harper","Hazel","Henry","Isaac",
    "Isabella","Jack","Jackson","Jacob","James","Jaxon","John","Joseph","Joshua","Julian",
    "Layla","Levi","Liam","Lincoln","Logan","Lucas","Luna","Madison","Mason","Mateo",
    "Mia","Michael","Mila","Nathan","Noah","Nora","Olivia","Owen","Penelope","Riley",
    "Samuel","Scarlett","Sebastian","Sofia","Sophia","Stella","Theodore","Thomas","Victoria","Violet",
    "William","Wyatt","Zoe","Zachary","Aurora","Eva","Gianna","Ivy","Jade","Kai",
    "Kinsley","Leo","Lily","Luca","Naomi","Paisley","Piper","Quinn","Ruby","Sadie",
    "Sage","Skylar","Taylor","Tristan","Valentina","Vivian","Wesley","Xavier","Zoey","Adrian",
]

LAST_NAMES_POOL: List[str] = [
    "Adams","Allen","Anderson","Baker","Barnes","Bell","Bennett","Brooks","Brown","Butler",
    "Campbell","Carter","Clark","Collins","Cook","Cooper","Cox","Davis","Diaz","Edwards",
    "Evans","Fisher","Flores","Foster","Garcia","Gonzalez","Gray","Green","Hall","Harris",
    "Hayes","Henderson","Hernandez","Hill","Howard","Hughes","Jackson","James","Jenkins","Johnson",
    "Jones","Kelly","King","Lee","Lewis","Long","Lopez","Martin","Martinez","Miller",
    "Mitchell","Moore","Morgan","Morris","Murphy","Nelson","Nguyen","Parker","Perez","Peterson",
    "Phillips","Powell","Ramirez","Reed","Richardson","Rivera","Roberts","Robinson","Rodriguez","Rogers",
    "Ross","Russell","Sanchez","Sanders","Scott","Simmons","Smith","Stewart","Taylor","Thomas",
    "Thompson","Torres","Turner","Walker","Ward","Washington","Watson","White","Williams","Wilson",
    "Wood","Wright","Young","Alexander","Brady","Burke","Chavez","Coleman","Cross","Duncan",
]

COMPANY_NAMES: List[str] = [
    "Acorn Analytics",
    "Blue Ridge Logistics",
    "Cedarline Health",
    "DeltaNova Systems",
    "Evergreen Retail Group",
    "ForgeWorks Manufacturing",
    "Golden Harbor Finance",
    "Horizon Education Services",
    "Ironclad Security",
    "Juniper Cloud Labs",
]


def _pick_unique(pool: List[str], count: int, rng: random.Random) -> List[str]:
    if len(pool) < count:
        raise ValueError(f"Pool too small: need {count}, have {len(pool)}")
    return rng.sample(pool, count)


def generate_employers() -> Dict[str, dict]:
    employer_ids = [f"EMP{idx:03d}" for idx in range(1, 11)]
    employers: Dict[str, dict] = {}
    if len(COMPANY_NAMES) != len(employer_ids):
        raise ValueError(f"Expected {len(employer_ids)} company names, got {len(COMPANY_NAMES)}")
    for employer_id, company_name in zip(employer_ids, COMPANY_NAMES):
        employers[employer_id] = {"companyName": company_name, "employerID": employer_id}
    return employers


def generate_insurance(employer_ids: List[str], rng: random.Random) -> Dict[str, List[int]]:
    insurance: Dict[str, List[int]] = {}
    for employer_id in employer_ids:
        # Two distinct-ish plan numbers for an A-M vs N-Z split
        plan_am = rng.randint(1000, 1999)
        plan_nz = rng.randint(2000, 2999)
        insurance[employer_id] = [plan_am, plan_nz]
    return insurance


def generate_clients(employer_ids: List[str], rng: random.Random) -> List[dict]:
    first_names = _pick_unique(FIRST_NAMES_POOL, 100, rng)
    last_names = _pick_unique(LAST_NAMES_POOL, 100, rng)

    clients: List[dict] = []
    for idx in range(1, 101):
        clients.append(
            {
                "firstName": first_names[idx - 1],
                "lastName": last_names[idx - 1],
                "employeeID": f"E{idx:05d}",
                "employerID": rng.choice(employer_ids),
            }
        )
    return clients


def main() -> int:
    parser = argparse.ArgumentParser(description="Generate clients/employer/insurance JSON demo data")
    parser.add_argument(
        "--out-dir",
        default=os.path.join(os.path.dirname(__file__), "."),
        help="Directory to write JSON files into (default: this script's directory)",
    )
    parser.add_argument(
        "--seed",
        type=int,
        default=1337,
        help="Random seed for reproducible output (default: 1337)",
    )
    args = parser.parse_args()

    rng = random.Random(args.seed)

    out_dir = os.path.abspath(args.out_dir)
    os.makedirs(out_dir, exist_ok=True)

    employers = generate_employers()
    employer_ids = list(employers.keys())

    insurance = generate_insurance(employer_ids, rng)
    clients = generate_clients(employer_ids, rng)

    with open(os.path.join(out_dir, "clients.json"), "w", encoding="utf-8") as f:
        json.dump(clients, f, indent=2)
        f.write("\n")

    with open(os.path.join(out_dir, "employer.json"), "w", encoding="utf-8") as f:
        json.dump(employers, f, indent=2)
        f.write("\n")

    with open(os.path.join(out_dir, "insurance.json"), "w", encoding="utf-8") as f:
        json.dump(insurance, f, indent=2)
        f.write("\n")

    return 0


if __name__ == "__main__":
    raise SystemExit(main())

import os
import xml.etree.ElementTree as ET

job_name_list = ["1-7","2-2","3-2","4-7","5-1","6-2","7-2","8-5","9-3","10-0","11-2","12-3","13-4","14-0","15-4","16-3","17-5","18-1","19-7","20-4","21-1","22-7","23-4","24-3","25-7","26-8","27-7","28-0","29-1","30-0"]
problem_name_list = [
    "org.uma.jmetal.problem.multiobjective.ecole.Erc1",
    "org.uma.jmetal.problem.multiobjective.ecole.Erc2",
    "org.uma.jmetal.problem.multiobjective.ecole.Erc3",
    "org.uma.jmetal.problem.multiobjective.ecole.Erc4",
    "org.uma.jmetal.problem.multiobjective.ecole.Erc5",
    "org.uma.jmetal.problem.multiobjective.ecole.Erc6",
    "org.uma.jmetal.problem.multiobjective.ecole.Erc7",
    "org.uma.jmetal.problem.multiobjective.ecole.Erc8",
    "org.uma.jmetal.problem.multiobjective.ecole.Erc9",
    "org.uma.jmetal.problem.multiobjective.ecole.Erc10",
    "org.uma.jmetal.problem.multiobjective.ecole.Erc11",
    "org.uma.jmetal.problem.multiobjective.ecole.Erc12",
    "org.uma.jmetal.problem.multiobjective.ecole.Erc13",
    "org.uma.jmetal.problem.multiobjective.ecole.Erc14",
    "org.uma.jmetal.problem.multiobjective.ecole.Erc15",
    "org.uma.jmetal.problem.multiobjective.ecole.Erc16",
    "org.uma.jmetal.problem.multiobjective.ecole.Erc17",
    "org.uma.jmetal.problem.multiobjective.ecole.Erc18",
    "org.uma.jmetal.problem.multiobjective.ecole.Erc19",
    "org.uma.jmetal.problem.multiobjective.ecole.Erc20",
    "org.uma.jmetal.problem.multiobjective.ecole.Erc21",
    "org.uma.jmetal.problem.multiobjective.ecole.Erc22",
    "org.uma.jmetal.problem.multiobjective.ecole.Erc23",
    "org.uma.jmetal.problem.multiobjective.ecole.Erc24",
    "org.uma.jmetal.problem.multiobjective.ecole.Erc25",
    "org.uma.jmetal.problem.multiobjective.ecole.Erc26",
    "org.uma.jmetal.problem.multiobjective.ecole.Erc27",
    "org.uma.jmetal.problem.multiobjective.ecole.Erc28",
    "org.uma.jmetal.problem.multiobjective.ecole.Erc29",
    "org.uma.jmetal.problem.multiobjective.ecole.Erc30"
]
population_size_list = [10, 20]

destinate_path = "workloads-2d"

for j_idx, problem_mame in enumerate(problem_name_list):
    job_name = job_name_list[j_idx]
    job_path = f'{destinate_path}/jobId_{job_name}/evo'
    if not os.path.exists(job_path):
        os.makedirs(job_path)
    for population_size in population_size_list:
        exec_cmd = f'mvn -X exec:java -Dexec.mainClass="org.uma.jmetal.runner.multiobjective.NSGAIIIntegerRunner" -Dexec.args="{problem_mame} {population_size}"'
        exec_mv_cmd_1 = f'mv FUN.tsv {job_path}/{population_size}_data'
        exec_mv_cmd_2 = f'mv VAR.tsv {job_path}/{population_size}_config'
        exec_mv_cmd_3 = f'mv jMetal.log {job_path}/{population_size}_jMetal.log'
        os.system(exec_cmd)
        os.system(exec_mv_cmd_1)
        os.system(exec_mv_cmd_2)
        os.system(exec_mv_cmd_3)
        # print(exec_cmd)
        # print(exec_mv_cmd_1)
        # print(exec_mv_cmd_2)
        # print(exec_mv_cmd_3)

        root = ET.parse(f'{job_path}/{population_size}_jMetal.log').getroot()
        notes = root.findall("record")[1].find('message').text
        with open(f'{job_path}/{population_size}_notes', "w") as f:
            f.write(f"{notes}; from jMetal.log with problemName={problem_mame}, populationSize={population_size}\n")

        print(f'{job_name} on {problem_mame}, {population_size} has finished.')

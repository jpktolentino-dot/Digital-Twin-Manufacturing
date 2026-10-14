import type { MachineInfo } from "@/types/machine";

const mockMachineInfo: MachineInfo[] = [
  { id: "1", label: "Informação 1", value: 55 },
  { id: "2", label: "Informação 2", value: 234 },
  { id: "3", label: "Informação 3", value: 10 },
  { id: "4", label: "Informação 4", value: 30 },
];


export async function getMachineInfo(): Promise<MachineInfo[]> {
  return mockMachineInfo;
}
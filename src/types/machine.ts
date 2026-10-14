export type MachineInfo = {
  id: string;
  label: string;
  value: number;
};

export type InfoRow = {
  id: string;
  label: string;
  value: string | number;
};

export type ProductionOrder = {
  numero: number;
  estagioAtual: string;
  produtoId: number;
  operadorId: number;
};

export type Product = {
  id: number;
  nome: string;
  codigo: number;
  especificacoes: string;
};
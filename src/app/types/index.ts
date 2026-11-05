// src/types/index.ts


export type TipoUsuario = "ADMINISTRADOR" | "OPERADOR";


export type EstadoMaquina =
  | "OPERANDO"
  | "PARADA"
  | "FALHA"
  | "MANUTENCAO"



export type EstagioProducao =
  | "PLANEJADA"
  | "EM_PRODUCAO"
  | "PAUSADA"
  | "CONCLUIDA"
  | "CANCELADA";

export interface Usuario {
  id: number;
  nome: string;
  email: string;
  tipo: TipoUsuario;
  ativo: boolean;
}

export interface UsuarioRequest {
  nome: string;
  email: string;
  senha: string;
}

export interface UsuarioUpdateRequest {
  nome: string;
  email: string;
  senha?: string;
  tipoUsuario: TipoUsuario;
  ativo: boolean;
}

// ---------- Setores ----------
export interface Setor {
  id: number;
  nome: string;
  codigo: string;
  ativo: boolean;
}

export interface SetorRequest {
  nome: string;
  codigo: string;
}

// ---------- Máquinas ----------
export interface Maquina {
  id: number;
  nome: string;
  codigo: string;
  especificacoes: string;
  estadoAtual: EstadoMaquina;
  setor: Setor;
  operadorId: number | null;
  ativo: boolean;
}

export interface MaquinaRequest {
  nome: string;
  codigo: string;
  especificacoes: string;
  estadoAtual: EstadoMaquina;
  setorId: number;
  operadorId?: number | null;
}


export interface AlterarEstadoMaquinaRequest {
  estadoAtual: EstadoMaquina;
}


export interface Produto {
  id: number;
  nome: string;
  codigo: string;
  especificacoes: string;
  ativo: boolean;
}

export interface ProdutoRequest {
  nome: string;
  codigo: string;
  especificacoes: string;
}


export interface OrdemProducao {
  id: number;
  numero: string;
  quantidadePlanejada: number;
  estagioAtual: EstagioProducao;
  produtoId: number;
  produtoNome: string;
  operadorId: number;
  operadorNome: string;
  maquinasIds: number[];
}

export interface OrdemProducaoRequest {
  numero: string;
  quantidadePlanejada: number;
  produtoId: number;
  operadorId: number;
  maquinasIds: number[];
}


export interface AlterarEstagioRequest {
  estagioAtual: EstagioProducao;
}


export interface Evento {
  id: number;
  categoria: string;
  descricao: string;
  impactoEstimado: number;
  dataHora: string; // ISO 8601
  maquinaId: number;
  maquinaNome: string;
  ordemProducaoId: number;
  numeroOrdem: string;
  usuarioId: number;
  usuarioNome: string;
}

export interface EventoRequest {
  categoria: string;
  descricao: string;
  impactoEstimado: number;
  dataHora: string; // ISO 8601
  maquinaId: number;
  ordemProducaoId: number;
  usuarioId: number;
}


export interface HistoricoMaquina {
  id: number;
  estadoAnterior: EstadoMaquina;
  estadoNovo: EstadoMaquina;
  dataHoraInicio: string;
  dataHoraFim: string | null; 
  maquinaId: number;
  maquinaNome: string;
}


export interface KpiMaquina {
  maquinaId: number;
  maquinaNome: string;
  inicio: string;
  fim: string;
  tempoOperacaoSegundos: number;
  tempoParadaSegundos: number;
  tempoFalhaSegundos: number;
  tempoManutencaoSegundos: number;
  tempoIndisponivelSegundos: number;
  tempoMonitoradoSegundos: number;
  tempoSemHistoricoSegundos: number;
  disponibilidadePercentual: number;
}
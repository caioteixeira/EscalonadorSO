import java.util.LinkedList;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Escalonador{
	static BCP[] tabelaDeProcessos;
	static LinkedList<Integer> prontos = new LinkedList<Integer>();
	static LinkedList<Integer> bloqueados = new LinkedList<Integer>();
	static int quantum = 0;
	static float numTrocas = 0; //-1 para ignorar primeiro processo
	static float numIntrucoes = 0;
	static float numQuanta = 0;

	public static void main(String[] args)
	{
		try{

			//Carrega tempo de quantum
			File arquivoQuantum = new File("processos/quantum.txt");
			Scanner sc = new Scanner(arquivoQuantum);
			quantum = sc.nextInt();
			sc.close();
			//System.out.println(quantum);
			
			//Inicializa Writers para log
			String nomeDoArquivo = "log"+(quantum < 10?"0"+quantum:quantum)+ ".txt";
			FileWriter writer = new FileWriter(nomeDoArquivo);
			PrintWriter saida = new PrintWriter(writer);


			//Inicializa processos
			tabelaDeProcessos = new BCP[10];
			for(int i = 0; i < 10; i++)
			{
				String n = i==9?"10":"0"+(i+1);
				tabelaDeProcessos[i] = new BCP("processos/"+n+".txt");
				prontos.add(i);
				saida.println("Carregando " + tabelaDeProcessos[i].nome);
			}

			//ESCALONADOR
			while(prontos.size() > 0 || bloqueados.size() > 0)
			{
				//Atualiza processo
				int proximo = -1;

				//Estado do processo apos ultima instrucao executada (antes da interrupcao)
				Estado estadoSaida = Estado.Pronto;
				if(prontos.size() > 0)
				{
					proximo = prontos.remove();
					saida.println("Executando "+tabelaDeProcessos[proximo].nome);
					int i;

					boolean voltouDeBloqueio = tabelaDeProcessos[proximo].terminouES;

					//Executa instrucoes
					for(i = 1; i <= quantum; i++)
					{
						estadoSaida = tabelaDeProcessos[proximo].roda();

						if(estadoSaida == Estado.Fim)
						{
							break;
						}

						if(estadoSaida == Estado.Bloqueado)
						{
							saida.println("E/S iniciada em " + tabelaDeProcessos[proximo].nome);
							//saida.println("Interrompendo "+ tabelaDeProcessos[proximo].nome + " após " + (i+1) + (i>1?" intruções.":" instrução."));
							break;
						}
					}

					if(i>=quantum) //Normaliza i para caso seja interrompido pelo escalonador
						i--;

					if(estadoSaida != Estado.Fim)
					{
						saida.println("Interrompendo "+ tabelaDeProcessos[proximo].nome + " após " + i + (i>1?" intruções.":" instrução."));
					}
					numTrocas++;

					//Soma num de intrucoes
					if(!voltouDeBloqueio)
					{
						numIntrucoes += i;
						//saida.println("Contou " + i);
					}
					else if(i > 1)
					{
						//saida.println("Contou " + (i-1));
						numIntrucoes += i-1; //Garante que instruções executadas depois da E/S sejam contadas
					}

					//Soma numero de quanta
					numQuanta++;
				}

				

				switch(estadoSaida)
				{
					case Rodando:
						
						tabelaDeProcessos[proximo].preempsao();
						prontos.add(proximo);
						break;
					case Bloqueado:
					
						bloqueados.add(proximo);
						break;

					case Fim:
					
						saida.println(tabelaDeProcessos[proximo].nome + " terminado. X="+tabelaDeProcessos[proximo].x + ". Y="+tabelaDeProcessos[proximo].y);
						break;
				}


				//Atualiza bloqueados
				for(int i = 0; i < bloqueados.size(); i++)
				{
					int bloqueado = bloqueados.get(i);
					Estado estadoES = Estado.Bloqueado;

					if(bloqueado != proximo)
						estadoES = tabelaDeProcessos[bloqueado].atualizaES();

					if(estadoES == Estado.Pronto && bloqueado != proximo)
					{
						bloqueados.remove(i);
						i--; //Garante que bloqueado removido nao quebre ordem de iteracao
						prontos.add(bloqueado);
					}
				}

			}

			//Garante que fim do último programa não seja contado
			numTrocas--;

			//Estatisticas finais
			saida.println("MEDIA DE TROCAS: " + String.format("%.2f", numTrocas/10));
			saida.println("MEDIA DE INSTRUCOES: " + String.format("%.2f", numIntrucoes/numTrocas));
			saida.print("QUANTUM: " + quantum);

			saida.close();
			writer.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}		
	}
}

enum Estado
{
	Pronto,
	Rodando,
	Bloqueado,
	Fim;
}

class BCP{
	public String nome;
	int programCounter = 0;
	Estado estado;
	String x = "0";
	String y = "0";
	LinkedList<String> comandos;

	public boolean terminouES = false;
	int tempoDeEspera = 0;

	//Inicializa bloco de controle de processo (carrega de arquivo)
	BCP(String programa)
	{
		comandos = new LinkedList<String>();

		File arquivo = new File(programa);

		try
		{
			Scanner sc = new Scanner(arquivo);
			nome = sc.nextLine();

			while(sc.hasNextLine())
			{
				comandos.add(sc.nextLine());
				//System.out.println(sc.nextLine());
			}
			//System.out.println("C"+comandos.size());

			sc.close();
		}
		catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	public Estado atualizaES()
	{
		tempoDeEspera--;
		//System.out.println(nome);
		if(tempoDeEspera <= 0)
		{
			terminouES = true;
			estado = Estado.Pronto;
		}
		else
			estado = Estado.Bloqueado;

		return estado;
	}

	public Estado roda()
	{
		estado = Estado.Rodando;
		String comando = comandos.get(programCounter);
		//System.out.println(comando);

		if(comando.equals("SAIDA"))
		{
			estado = Estado.Fim;
		}
		if(comando.equals("E/S"))
		{
			if(terminouES)
			{
				terminouES = false;
				estado = Estado.Pronto;
			}
			else
			{
				estado = Estado.Bloqueado;
				tempoDeEspera = 2;
				return estado;
			}
		}
		if(comando.startsWith("X") || comando.startsWith("Y"))
		{
			String[] a = comando.split("=");
			if(a[0].equals("X"))
				x = a[1];
			else
				y = a[1];
		}

		programCounter++;
		return estado;
	}

	public void preempsao()
	{
		estado = Estado.Pronto;
	}
}

import java.util.LinkedList;

public class Escalonador{

	BCP[] tabelaDeProcessos;
	LinkedList<BCP> prontos;
	LinkedList<BCP> bloqueados;

	
	public static void main(String[] args)
	{
		//Código...
	} 
}

enum Estado
{
	Pronto,
	Rodando,
	Bloqueado;
}

class BCP{
	int programCounter = 0;
	Estado estado;
	int x = 0;
	int y = 0;
	String[] comandos;
}
